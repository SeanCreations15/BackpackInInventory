package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import com.sean.backpackininventory.menu.IntegratedMenuOpener;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectBackpackPayload(UUID uuid) implements CustomPacketPayload {
    public static final Type<SelectBackpackPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "select_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectBackpackPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> buffer.writeUUID(payload.uuid),
                    buffer -> new SelectBackpackPayload(buffer.readUUID()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SelectBackpackPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof IntegratedBackpackMenu) {
            IntegratedMenuOpener.open(player, Optional.of(payload.uuid));
        }
    }
}
