package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.client.ContainerCompanionClient;
import com.sean.backpackininventory.menu.CompanionBackpackData;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AttachContainerPayload(int menuId, UUID uuid, int count, int columns, ItemStack icon)
        implements CustomPacketPayload {
    public static final Type<AttachContainerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "attach_container"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachContainerPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.menuId);
                buffer.writeUUID(value.uuid);
                buffer.writeVarInt(value.count);
                buffer.writeVarInt(value.columns);
                ItemStack.STREAM_CODEC.encode(buffer, value.icon);
            },
            buffer -> new AttachContainerPayload(buffer.readVarInt(), buffer.readUUID(), buffer.readVarInt(),
                    buffer.readVarInt(), ItemStack.STREAM_CODEC.decode(buffer)));

    public static AttachContainerPayload from(int menuId, CompanionBackpackData data) {
        return new AttachContainerPayload(menuId, data.uuid(), data.count(), data.columns(), data.icon());
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(AttachContainerPayload payload, IPayloadContext context) {
        ContainerCompanionClient.attach(payload);
    }
}
