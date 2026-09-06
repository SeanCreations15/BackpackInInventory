package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlaceRecipePayload(int menuId, ResourceLocation recipeId, boolean craftAll) implements CustomPacketPayload {
    public static final Type<PlaceRecipePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "place_recipe"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceRecipePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.menuId);
                buffer.writeResourceLocation(payload.recipeId);
                buffer.writeBoolean(payload.craftAll);
            }, buffer -> new PlaceRecipePayload(buffer.readVarInt(), buffer.readResourceLocation(), buffer.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    public static void handle(PlaceRecipePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof IntegratedBackpackMenu menu)
                || menu.containerId != payload.menuId) {
            return;
        }
        player.level().getRecipeManager().byKey(payload.recipeId).ifPresent(recipe -> {
            if (recipe.value().getType() == RecipeType.CRAFTING) {
                menu.recipeBookAdapter().handlePlacement(payload.craftAll, (RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) recipe, player);
            }
        });
    }
}
