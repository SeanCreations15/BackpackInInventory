package com.sean.backpackininventory.menu;

import com.sean.backpackininventory.init.ModMenus;
import java.util.ArrayList;
import java.util.List;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

public class IntegratedBackpackMenu extends BackpackContainer {
    public static final int VANILLA_EXTRA_SLOT_COUNT = 10;
    private final List<BackpackDescriptor> backpacks;
    private CraftingContainer craftSlots;
    private ResultContainer resultSlots;
    private int vanillaExtraStart;
    private int curiosStart;
    private int curiosSlotCount;

    public IntegratedBackpackMenu(int containerId, Player player, BackpackContext context, List<BackpackDescriptor> backpacks) {
        super(containerId, player, context);
        this.backpacks = List.copyOf(backpacks);
    }

    public static IntegratedBackpackMenu fromBuffer(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        BackpackContext context = BackpackContext.fromBuffer(buffer, inventory.player.level());
        int count = buffer.readVarInt();
        List<BackpackDescriptor> backpacks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            backpacks.add(BackpackDescriptor.read(buffer));
        }
        return new IntegratedBackpackMenu(containerId, inventory.player, context, backpacks);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenus.INTEGRATED_BACKPACK.get();
    }

    public List<BackpackDescriptor> backpacks() {
        return backpacks;
    }

    @Override
    protected void initSlotsAndContainers(Player player, int storageItemSlotIndex, boolean shouldLockStorageItemSlot,
            List<Slot> ignoredExtraSlots) {
        addStorageInventorySlots();
        addPlayerInventorySlots(player.getInventory(), storageItemSlotIndex, shouldLockStorageItemSlot);
        vanillaExtraStart = getInventorySlotsSize();
        addVanillaInventorySlots(player);
        curiosStart = getInventorySlotsSize();
        curiosSlotCount = CuriosMenuCompat.addSlots(this, player);

        UpgradeHandler upgradeHandler = storageWrapper.getUpgradeHandler();
        for (int slotIndex = 0; slotIndex < upgradeHandler.getSlots(); slotIndex++) {
            addUpgradeSlot(instantiateUpgradeSlot(upgradeHandler, slotIndex));
        }
        addUpgradeSettingsContainers(player);
    }

    private void addVanillaInventorySlots(Player player) {
        Inventory inventory = player.getInventory();
        craftSlots = new TransientCraftingContainer(this, 2, 2);
        resultSlots = new ResultContainer();
        addExtraSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 0, 0));
        for (int i = 0; i < 4; i++) {
            addExtraSlot(new Slot(craftSlots, i, 0, 0));
        }

        EquipmentSlot[] equipmentSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        ResourceLocation[] icons = {InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
                InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};
        for (int i = 0; i < equipmentSlots.length; i++) {
            EquipmentSlot equipmentSlot = equipmentSlots[i];
            ResourceLocation icon = icons[i];
            addExtraSlot(new Slot(inventory, 39 - i, 0, 0) {
                @Override
                public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                    player.onEquipItem(equipmentSlot, oldStack, newStack);
                    super.setByPlayer(newStack, oldStack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.canEquip(equipmentSlot, player);
                }

                @Override
                public boolean mayPickup(Player picker) {
                    ItemStack stack = getItem();
                    return (stack.isEmpty() || picker.isCreative()
                            || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(picker);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
                }
            });
        }

        addExtraSlot(new Slot(inventory, 40, 0, 0) {
            @Override
            public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                player.onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
                super.setByPlayer(newStack, oldStack);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    public int vanillaExtraStart() {
        return vanillaExtraStart;
    }

    void addIntegratedExtraSlot(Slot slot) {
        addExtraSlot(slot);
    }

    public int curiosStart() {
        return curiosStart;
    }

    public int curiosSlotCount() {
        return curiosSlotCount;
    }

    public boolean isSelectedBackpack(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return false;
        }
        var selectedUuid = storageWrapper.getContentsUuid();
        var stackUuid = BackpackWrapper.fromExistingData(stack).flatMap(wrapper -> wrapper.getContentsUuid());
        return selectedUuid.isPresent() && selectedUuid.equals(stackUuid);
    }

    public CraftingContainer craftSlots() {
        return craftSlots;
    }

    public RecipeBookAdapter recipeBookAdapter() {
        return new RecipeBookAdapter(this);
    }

    boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) {
        return recipe.value().matches(craftSlots.asCraftInput(), player.level());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        int storageSlots = getNumberOfStorageInventorySlots();
        if (index >= curiosStart && index < curiosStart + curiosSlotCount) {
            return quickMoveCurioStack(player, index, storageSlots);
        }
        if (index < storageSlots || index >= storageSlots + 36) {
            return super.quickMoveStack(player, index);
        }

        Slot source = getSlot(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(sourceStack);
        int armorTarget = equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                ? vanillaExtraStart + 5 + (3 - equipmentSlot.getIndex()) : -1;
        if (armorTarget >= 0 && !getSlot(armorTarget).hasItem()
                && moveItemStackTo(sourceStack, armorTarget, armorTarget + 1, false)) {
            // Equipped using vanilla rules.
        } else if (equipmentSlot == EquipmentSlot.OFFHAND && !getSlot(vanillaExtraStart + 9).hasItem()
                && moveItemStackTo(sourceStack, vanillaExtraStart + 9, vanillaExtraStart + 10, false)) {
            // Equipped to the offhand.
        } else if (moveIntoFirstMatchingCurioSlot(sourceStack)) {
            // Equipped into the first compatible Curios slot.
        } else if (!moveItemStackTo(sourceStack, 0, storageSlots, false)) {
            int relative = index - storageSlots;
            boolean moved = relative < 27
                    ? moveItemStackTo(sourceStack, storageSlots + 27, storageSlots + 36, false)
                    : moveItemStackTo(sourceStack, storageSlots, storageSlots + 27, false);
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY, original);
        } else {
            source.setChanged();
        }
        if (sourceStack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        source.onTake(player, sourceStack);
        return original;
    }

    private ItemStack quickMoveCurioStack(Player player, int index, int storageSlots) {
        Slot source = getSlot(index);
        if (!source.hasItem() || !source.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack sourceStack = source.getItem();
        ItemStack original = sourceStack.copy();
        if (!moveItemStackTo(sourceStack, storageSlots, storageSlots + 36, false)
                && !moveItemStackTo(sourceStack, 0, storageSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (sourceStack.isEmpty()) {
            source.setByPlayer(ItemStack.EMPTY, original);
        } else {
            source.setChanged();
        }
        source.onTake(player, sourceStack);
        return original;
    }

    private boolean moveIntoFirstMatchingCurioSlot(ItemStack stack) {
        for (int index = curiosStart; index < curiosStart + curiosSlotCount; index++) {
            Slot slot = getSlot(index);
            if (!slot.hasItem() && slot.mayPlace(stack) && moveItemStackTo(stack, index, index + 1, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isUpgradeSettingsSlot(int index) {
        return index >= getFirstUpgradeSlot() + getNumberOfUpgradeSlots() && index < getTotalSlotsNumber();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == craftSlots && !player.level().isClientSide) {
            updateCraftingResult();
        }
    }

    private void updateCraftingResult() {
        CraftingInput input = craftSlots.asCraftInput();
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> recipe = player.level().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, player.level());
        if (recipe.isPresent() && resultSlots.setRecipeUsed(player.level(), serverPlayer, recipe.get())) {
            ItemStack assembled = recipe.get().value().assemble(input, player.level().registryAccess());
            if (assembled.isItemEnabled(player.level().enabledFeatures())) {
                result = assembled;
            }
        }
        resultSlots.setItem(0, result);
        setRemoteSlot(vanillaExtraStart, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), vanillaExtraStart, result));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (resultSlots != null) {
            resultSlots.clearContent();
        }
        if (!player.level().isClientSide && craftSlots != null) {
            clearContainer(player, craftSlots);
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }
}
