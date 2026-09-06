package com.sean.backpackininventory.client;

import com.sean.backpackininventory.network.OpenInventoryPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

/** Backpack settings screen whose back action restores the combined inventory. */
final class IntegratedBackpackSettingsScreen extends BackpackSettingsScreen {
    IntegratedBackpackSettingsScreen(SettingsContainerMenu<?> menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void sendStorageInventoryScreenOpenMessage() {
        PacketDistributor.sendToServer(new OpenInventoryPayload());
    }
}
