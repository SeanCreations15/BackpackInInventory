package com.sean.backpackininventory.menu;

import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.ItemStack;

/** Bridges vanilla recipe placement to the crafting slots embedded in the backpack menu. */
public final class RecipeBookAdapter extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    private final IntegratedBackpackMenu delegate;

    public RecipeBookAdapter(IntegratedBackpackMenu delegate) {
        super(delegate.getType(), delegate.containerId);
        this.delegate = delegate;
        int start = delegate.vanillaExtraStart();
        for (int i = 0; i < 5; i++) {
            Slot actual = delegate.getSlot(start + i);
            addSlot(new Slot(actual.container, actual.getContainerSlot(), actual.x, actual.y));
        }
    }

    @Override
    public Slot getSlot(int index) {
        return delegate.getSlot(delegate.vanillaExtraStart() + index);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        delegate.craftSlots().fillStackedContents(contents);
    }

    @Override
    public void clearCraftingContent() {
        delegate.craftSlots().clearContent();
    }

    @Override
    public boolean recipeMatches(RecipeHolder<CraftingRecipe> recipe) {
        return delegate.recipeMatches(recipe);
    }

    @Override public int getResultSlotIndex() { return 0; }
    @Override public int getGridWidth() { return 2; }
    @Override public int getGridHeight() { return 2; }
    @Override public int getSize() { return 5; }
    @Override public RecipeBookType getRecipeBookType() { return RecipeBookType.CRAFTING; }
    @Override public boolean shouldMoveToInventory(int slotIndex) { return slotIndex != 0; }
    @Override public boolean stillValid(Player player) { return delegate.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
