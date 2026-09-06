package com.sean.backpackininventory.compat.jei;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.init.ModMenus;
import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Optional JEI bridge for the vanilla 2x2 crafting grid in the integrated menu. */
@JeiPlugin
public final class BackpackInInventoryJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            BackpackInInventory.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new IntegratedCraftingTransferInfo());
    }

    private static final class IntegratedCraftingTransferInfo implements
            IRecipeTransferInfo<IntegratedBackpackMenu, RecipeHolder<CraftingRecipe>> {
        @Override
        public Class<? extends IntegratedBackpackMenu> getContainerClass() {
            return IntegratedBackpackMenu.class;
        }

        @Override
        public Optional<MenuType<IntegratedBackpackMenu>> getMenuType() {
            return Optional.of(ModMenus.INTEGRATED_BACKPACK.get());
        }

        @Override
        public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
            return RecipeTypes.CRAFTING;
        }

        @Override
        public boolean canHandle(IntegratedBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
            return recipe.value().canCraftInDimensions(2, 2);
        }

        @Override
        public List<Slot> getRecipeSlots(IntegratedBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
            int firstInput = menu.vanillaExtraStart() + 1;
            return menu.slots.subList(firstInput, firstInput + 4);
        }

        @Override
        public List<Slot> getInventorySlots(IntegratedBackpackMenu menu, RecipeHolder<CraftingRecipe> recipe) {
            int firstPlayerSlot = menu.getNumberOfStorageInventorySlots();
            return menu.slots.subList(firstPlayerSlot, firstPlayerSlot + 36);
        }
    }
}
