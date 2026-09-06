package com.sean.backpackininventory.network;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.menu.IntegratedMenuOpener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackSettingsContainerMenu;

public record OpenInventoryPayload(boolean equippedOnly) implements CustomPacketPayload {
    public OpenInventoryPayload() { this(false); }
    public static final Type<OpenInventoryPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(BackpackInInventory.MOD_ID, "open_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenInventoryPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> buf.writeBoolean(payload.equippedOnly()), buf -> new OpenInventoryPayload(buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenInventoryPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            boolean validSourceMenu = player.containerMenu instanceof InventoryMenu
                    || player.containerMenu instanceof BackpackSettingsContainerMenu;
            if (player.isCreative() || player.isSpectator() || !validSourceMenu
                    || !IntegratedMenuOpener.open(player, java.util.Optional.empty(), payload.equippedOnly())) {
                context.reply(new OpenVanillaPayload());
            }
        }
    }
}
