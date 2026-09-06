package com.sean.backpackininventory.mixin;

import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import com.sean.backpackininventory.network.PlaceRecipePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handlePlaceRecipe(ILnet/minecraft/world/item/crafting/RecipeHolder;Z)V"))
    private void backpackininventory$placeRecipe(MultiPlayerGameMode gameMode, int containerId,
            RecipeHolder<?> recipe, boolean craftAll) {
        if (Minecraft.getInstance().player != null
                && Minecraft.getInstance().player.containerMenu instanceof IntegratedBackpackMenu) {
            PacketDistributor.sendToServer(new PlaceRecipePayload(containerId, recipe.id(), craftAll));
        } else {
            gameMode.handlePlaceRecipe(containerId, recipe, craftAll);
        }
    }
}
