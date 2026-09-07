package com.sean.backpackininventory.menu;

import com.sean.backpackininventory.mixin.AbstractContainerMenuAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.UUID;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

/** Adds a synchronized backpack partition to an otherwise unchanged menu. */
public final class CompanionBackpackMenus {
    private static final Map<AbstractContainerMenu, CompanionBackpackData> ATTACHED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CompanionBackpackMenus() { }

    public static boolean isEligible(AbstractContainerMenu menu) {
        return !(menu instanceof InventoryMenu)
                && !(menu instanceof IntegratedBackpackMenu)
                && !(menu instanceof BackpackContainer)
                && !(menu instanceof SettingsContainerMenu<?>)
                && !menu.getClass().getName().equals(
                        "com.sean.backpackininventory.compat.storage.StorageBackpackMenu");
    }

    public static Optional<CompanionBackpackData> get(AbstractContainerMenu menu) {
        return Optional.ofNullable(ATTACHED.get(menu));
    }

    public static Optional<CompanionBackpackData> attachServer(
            AbstractContainerMenu menu, Player player, LocatedBackpack backpack) {
        CompanionBackpackData existing = ATTACHED.get(menu);
        if (existing != null) return Optional.of(existing);
        if (!isEligible(menu)) return Optional.empty();

        var wrapper = backpack.context().getBackpackWrapper(player);
        var handler = wrapper.getInventoryHandler();
        int count = handler.getSlots();
        if (count < 1 || count > 4096) return Optional.empty();
        int rows = Math.max(1, wrapper.getNumberOfSlotRows());
        int columns = CompanionBackpackData.columnsFor(count, rows);
        int start = menu.slots.size();
        for (int i = 0; i < count; i++) {
            ((AbstractContainerMenuAccessor) menu).backpackininventory$addSlot(
                    new SlotItemHandler(handler, i, -2000, -2000) {
                        @Override public boolean mayPlace(ItemStack stack) {
                            return !isBackpack(stack, backpack.uuid()) && super.mayPlace(stack);
                        }

                        @Override public int getMaxStackSize(ItemStack stack) {
                            return getMaxStackSize();
                        }
                    });
        }
        CompanionBackpackData data = new CompanionBackpackData(
                backpack.uuid(), start, count, columns, backpack.stack().copy());
        ATTACHED.put(menu, data);
        return Optional.of(data);
    }

    public static Optional<CompanionBackpackData> attachClient(AbstractContainerMenu menu, UUID uuid,
            int count, int columns, ItemStack icon) {
        CompanionBackpackData existing = ATTACHED.get(menu);
        if (existing != null) return Optional.of(existing);
        if (!isEligible(menu) || count < 1 || count > 4096 || columns < 1 || columns > 12) {
            return Optional.empty();
        }
        int start = menu.slots.size();
        SimpleContainer items = new SimpleContainer(count);
        for (int i = 0; i < count; i++) {
            ((AbstractContainerMenuAccessor) menu).backpackininventory$addSlot(
                    new Slot(items, i, -2000, -2000) {
                        @Override public int getMaxStackSize() { return Integer.MAX_VALUE; }
                        @Override public int getMaxStackSize(ItemStack stack) { return Integer.MAX_VALUE; }
                    });
        }
        CompanionBackpackData data = new CompanionBackpackData(uuid, start, count, columns, icon.copy());
        ATTACHED.put(menu, data);
        return Optional.of(data);
    }

    public static boolean handleClick(AbstractContainerMenu menu, int slotId, int button,
            ClickType clickType, Player player) {
        CompanionBackpackData data = ATTACHED.get(menu);
        if (data == null) return false;
        if (!player.level().isClientSide && BackpackLocator.find(player).stream().noneMatch(backpack ->
                backpack.uuid().equals(data.uuid())
                        && backpack.context().getBackpackWrapper(player).getInventoryHandler().getSlots() == data.count())) {
            player.closeContainer();
            return true;
        }
        if (slotId >= 0 && slotId < menu.slots.size()
                && isBackpack(menu.getSlot(slotId).getItem(), data.uuid())) return true;
        if (clickType == ClickType.SWAP && button >= 0 && button < player.getInventory().getContainerSize()
                && isBackpack(player.getInventory().getItem(button), data.uuid())) return true;
        if (clickType != ClickType.QUICK_MOVE) return false;
        if (slotId < 0 || slotId >= menu.slots.size()) return true;

        Slot source = menu.getSlot(slotId);
        if (!source.mayPickup(player)) return true;
        ItemStack moved = quickMove(menu, player, source, slotId, data);
        int guard = 0;
        while (!moved.isEmpty() && ItemStack.isSameItem(source.getItem(), moved) && guard++ < 64) {
            moved = quickMove(menu, player, source, slotId, data);
        }
        return true;
    }

    private static ItemStack quickMove(AbstractContainerMenu menu, Player player, Slot source,
            int slotId, CompanionBackpackData data) {
        if (source.getItem().isEmpty() || isBackpack(source.getItem(), data.uuid())) return ItemStack.EMPTY;
        if (slotId >= data.start() && slotId < data.start() + data.count()) {
            return moveCustom(player, source, containerSlots(menu, player.getInventory(), data));
        }

        // Native menus must see their original slot count. Many chest-like menus use
        // slots.size() as the player-inventory boundary and would otherwise route
        // container items directly into the appended backpack partition.
        List<Slot> appended = new ArrayList<>(menu.slots.subList(data.start(), menu.slots.size()));
        menu.slots.subList(data.start(), menu.slots.size()).clear();
        ItemStack nativeResult;
        try {
            nativeResult = menu.quickMoveStack(player, slotId);
        } finally {
            menu.slots.addAll(appended);
        }

        // Player inventory only targets the opened container. For an opened
        // container, native logic gets first chance at the player inventory; only
        // a complete native failure is allowed to overflow into the backpack.
        if (source.container == player.getInventory() || !nativeResult.isEmpty() || source.getItem().isEmpty()) {
            return nativeResult;
        }
        return moveCustom(player, source, backpackSlots(menu, data));
    }

    private static List<Slot> containerSlots(AbstractContainerMenu menu, Inventory inventory,
            CompanionBackpackData data) {
        List<Slot> targets = new ArrayList<>();
        for (int i = 0; i < data.start(); i++) {
            Slot slot = menu.getSlot(i);
            if (slot.container != inventory) targets.add(slot);
        }
        return targets;
    }

    private static List<Slot> backpackSlots(AbstractContainerMenu menu, CompanionBackpackData data) {
        return new ArrayList<>(menu.slots.subList(data.start(), data.start() + data.count()));
    }

    private static ItemStack moveCustom(Player player, Slot source, List<Slot> targets) {
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (!moveInto(stack, targets)) return ItemStack.EMPTY;
        if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY, original);
        else source.setChanged();
        source.onTake(player, stack);
        return original;
    }

    private static boolean moveInto(ItemStack stack, List<Slot> targets) {
        boolean changed = false;
        if (stack.isStackable()) {
            for (Slot target : targets) {
                ItemStack present = target.getItem();
                if (present.isEmpty() || !ItemStack.isSameItemSameComponents(stack, present) || !target.mayPlace(stack)) continue;
                int maximum = target.getMaxStackSize(present);
                int moved = Math.min(stack.getCount(), maximum - present.getCount());
                if (moved <= 0) continue;
                present.grow(moved);
                stack.shrink(moved);
                target.setChanged();
                changed = true;
                if (stack.isEmpty()) return true;
            }
        }
        for (Slot target : targets) {
            if (!target.getItem().isEmpty() || !target.mayPlace(stack)) continue;
            int moved = Math.min(stack.getCount(), target.getMaxStackSize(stack));
            if (moved <= 0) continue;
            target.setByPlayer(stack.split(moved));
            target.setChanged();
            changed = true;
            if (stack.isEmpty()) break;
        }
        return changed;
    }

    public static boolean isBackpack(ItemStack stack, UUID uuid) {
        if (!(stack.getItem() instanceof BackpackItem)) return false;
        return BackpackWrapper.fromExistingData(stack).flatMap(wrapper -> wrapper.getContentsUuid())
                .filter(uuid::equals).isPresent();
    }
}
