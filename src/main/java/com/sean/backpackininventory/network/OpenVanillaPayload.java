package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.client.ClientInventoryInterceptor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenVanillaPayload() implements CustomPacketPayload {
    public static final Type<OpenVanillaPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "open_vanilla"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenVanillaPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenVanillaPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenVanillaPayload payload, IPayloadContext context) {
        ClientInventoryInterceptor.openVanilla();
    }
}
