package com.sean.backpackininventory.compat.storage;

import com.mojang.logging.LogUtils;
import com.sean.backpackininventory.init.ModMenus;
import com.sean.backpackininventory.menu.BackpackLocator;
import com.sean.backpackininventory.menu.CompanionBackpackData;
import com.sean.backpackininventory.menu.LocatedBackpack;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;
import org.slf4j.Logger;

/** The chest remains the primary Sophisticated menu, including its upgrades. */
public final class StorageBackpackMenu extends StorageContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();
    private record Opening(UUID uuid, IItemHandler handler, int count, int columns, ItemStack icon) { }
    private static final ThreadLocal<Opening> OPENING = new ThreadLocal<>();
    private Opening backpack;
    private int backpackStart;

    private StorageBackpackMenu(int id, Player player, BlockPos pos) {
        super(ModMenus.STORAGE_BACKPACK.get(), id, player, pos);
    }

    public static StorageBackpackMenu create(int id, Player player, BlockPos pos, LocatedBackpack selected) {
        var wrapper = selected.context().getBackpackWrapper(player);
        var handler = wrapper.getInventoryHandler();
        int rows = Math.max(1, wrapper.getNumberOfSlotRows());
        int columns = CompanionBackpackData.columnsFor(handler.getSlots(), rows);
        return construct(id, player, pos,
                new Opening(selected.uuid(), handler, handler.getSlots(), columns, selected.stack().copy()));
    }

    private static StorageBackpackMenu construct(int id, Player player, BlockPos pos, Opening opening) {
        OPENING.set(opening);
        try { return new StorageBackpackMenu(id, player, pos); }
        finally { OPENING.remove(); }
    }

    public static StorageBackpackMenu fromBuffer(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        UUID uuid = buffer.readUUID();
        int count = buffer.readVarInt();
        if (count < 1 || count > 4096) throw new IllegalArgumentException("Invalid backpack slot count");
        int columns = buffer.readVarInt();
        if (columns < 1 || columns > 12) throw new IllegalArgumentException("Invalid backpack column count");
        ItemStack icon = ItemStack.STREAM_CODEC.decode(buffer);
        return construct(id, inventory.player, pos, new Opening(uuid, null, count, columns, icon));
    }

    @Override
    protected void initSlotsAndContainers(Player player, int ignoredIndex, boolean ignoredLock, List<Slot> extras) {
        if (backpack == null) backpack = OPENING.get();
        if (backpack == null) throw new IllegalStateException("Missing backpack menu context");
        addStorageInventorySlots();
        int backingIndex = -1;
        for (int i = 0; i < 36; i++) if (isBacking(player.getInventory().getItem(i))) backingIndex = i;
        addPlayerInventorySlots(player.getInventory(), backingIndex, backingIndex >= 0);
        backpackStart = getInventorySlotsSize();
        SimpleContainer clientItems = new SimpleContainer(backpack.count());
        for (int i = 0; i < backpack.count(); i++) {
            if (backpack.handler() == null) {
                addExtraSlot(new Slot(clientItems, i, -2000, -2000) {
                    @Override public int getMaxStackSize() { return Integer.MAX_VALUE; }
                    @Override public int getMaxStackSize(ItemStack stack) { return Integer.MAX_VALUE; }
                });
            } else {
                addExtraSlot(new SlotItemHandler(backpack.handler(), i, -2000, -2000) {
                    @Override public boolean mayPlace(ItemStack stack) { return !isBacking(stack) && super.mayPlace(stack); }
                    @Override public int getMaxStackSize(ItemStack stack) { return getMaxStackSize(); }
                });
            }
        }
        var upgrades = storageWrapper.getUpgradeHandler();
        for (int i = 0; i < upgrades.getSlots(); i++) addUpgradeSlot(instantiateUpgradeSlot(upgrades, i));
        addUpgradeSettingsContainers(player);
    }

    public int backpackStart() { return backpackStart; }
    public int backpackCount() { return backpack.count(); }
    public int backpackColumns() { return backpack.columns(); }
    public ItemStack backpackIcon() { return backpack.icon(); }
    public boolean isBacking(ItemStack stack) {
        if (!(stack.getItem() instanceof net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem)) return false;
        return BackpackWrapper.fromExistingData(stack).flatMap(w -> w.getContentsUuid())
                .filter(backpack.uuid()::equals).isPresent();
    }

    @Override protected boolean isUpgradeSettingsSlot(int index) {
        return index >= getFirstUpgradeSlot() + getNumberOfUpgradeSlots() && index < getTotalSlotsNumber();
    }

    @Override public boolean hasSomethingMessedWithStorage() {
        if (isClientSide()) return false;
        int actual = slots.size();
        int storage = getNumberOfStorageInventorySlots();
        if (!StorageMenuSlotAccounting.isInvalid(false, actual, storage, backpackCount())) return false;

        // A joined/double storage can publish its authoritative inventory size just
        // after the replacement menu was built. Rebuild once before treating that
        // short-lived mismatch as corruption. Our own accounting intentionally does
        // not use Core's cumulative private extraSlotsSize counter.
        refreshAllSlots();
        int rebuiltActual = slots.size();
        int rebuiltStorage = getNumberOfStorageInventorySlots();
        boolean invalid = StorageMenuSlotAccounting.isInvalid(
                false, rebuiltActual, rebuiltStorage, backpackCount());
        if (invalid) {
            LOGGER.warn("Closing combined Sophisticated Storage menu after an invalid slot rebuild: "
                    + "beforeActual={}, beforeStorage={}, rebuiltActual={}, rebuiltStorage={}, backpack={}",
                    actual, storage, rebuiltActual, rebuiltStorage, backpackCount());
        }
        return invalid;
    }

    @Override public boolean stillValid(Player player) {
        if (!super.stillValid(player)) return false;
        if (player.level().isClientSide) return true;
        return BackpackLocator.find(player).stream().anyMatch(b -> b.uuid().equals(backpack.uuid())
                && b.context().getBackpackWrapper(player).getInventoryHandler().getSlots() == backpack.count());
    }

    @Override public void clicked(int index, int button, net.minecraft.world.inventory.ClickType type, Player player) {
        if (!player.level().isClientSide && !stillValid(player)) { player.closeContainer(); return; }
        if (index >= 0 && index < getTotalSlotsNumber() && isBacking(getSlot(index).getItem())) return;
        if (type == net.minecraft.world.inventory.ClickType.SWAP && button >= 0 && button < player.getInventory().getContainerSize()
                && isBacking(player.getInventory().getItem(button))) return;
        super.clicked(index, button, type, player);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot source = getSlot(index);
        if (!source.mayPickup(player) || isBacking(source.getItem())) return ItemStack.EMPTY;
        int storageEnd = getNumberOfStorageInventorySlots();
        boolean fromBackpack = index >= backpackStart && index < backpackStart + backpackCount();
        boolean fromPlayer = index >= storageEnd && index < storageEnd + 36;
        boolean fromStorage = index >= 0 && index < storageEnd;
        if (fromBackpack || fromPlayer || fromStorage) {
            ItemStack stack = source.getItem();
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack original = stack.copy();
            boolean moved;
            if (fromBackpack || fromPlayer) {
                moved = moveItemStackTo(stack, 0, storageEnd, false);
            } else {
                moved = moveItemStackTo(stack, storageEnd, storageEnd + 36, true);
                if (!stack.isEmpty()) {
                    moved |= moveItemStackTo(stack, backpackStart, backpackStart + backpackCount(), false);
                }
            }
            if (!moved) return ItemStack.EMPTY;
            if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY, original);
            else source.setChanged();
            source.onQuickCraft(stack, original);
            source.onTake(player, stack);
            return original;
        }
        return super.quickMoveStack(player, index);
    }
}
