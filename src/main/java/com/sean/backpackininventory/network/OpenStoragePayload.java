package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.compat.storage.StorageIntegration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenStoragePayload(int menuId, boolean equippedOnly) implements CustomPacketPayload {
    public static final Type<OpenStoragePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "open_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenStoragePayload> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> { buf.writeVarInt(value.menuId); buf.writeBoolean(value.equippedOnly); },
            buf -> new OpenStoragePayload(buf.readVarInt(), buf.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(OpenStoragePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && ModList.get().isLoaded("sophisticatedstorage"))
            StorageIntegration.open(player, payload.menuId, payload.equippedOnly);
    }
}
