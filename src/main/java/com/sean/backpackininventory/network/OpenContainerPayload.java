package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.init.ModAttachments;
import com.sean.backpackininventory.menu.BackpackLocator;
import com.sean.backpackininventory.menu.CompanionBackpackMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenContainerPayload(int menuId, boolean equippedOnly) implements CustomPacketPayload {
    public static final Type<OpenContainerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "open_container"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenContainerPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeVarInt(value.menuId);
                buffer.writeBoolean(value.equippedOnly);
            },
            buffer -> new OpenContainerPayload(buffer.readVarInt(), buffer.readBoolean()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenContainerPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || player.isSpectator() || player.containerMenu.containerId != payload.menuId()
                || !player.containerMenu.stillValid(player) || !player.containerMenu.getCarried().isEmpty()
                || !CompanionBackpackMenus.isEligible(player.containerMenu)) return;

        var existing = CompanionBackpackMenus.get(player.containerMenu);
        if (existing.isPresent()) {
            PacketDistributor.sendToPlayer(player, AttachContainerPayload.from(payload.menuId(), existing.get()));
            player.containerMenu.broadcastFullState();
            return;
        }

        var found = BackpackLocator.find(player).stream()
                .filter(backpack -> !payload.equippedOnly() || backpack.worn()).toList();
        var selected = BackpackLocator.select(
                found, player.getData(ModAttachments.SELECTED_BACKPACK.get()).selected());
        selected.flatMap(backpack -> CompanionBackpackMenus.attachServer(player.containerMenu, player, backpack))
                .ifPresent(data -> {
                    PacketDistributor.sendToPlayer(player, AttachContainerPayload.from(payload.menuId(), data));
                    player.containerMenu.broadcastFullState();
                });
    }
}
