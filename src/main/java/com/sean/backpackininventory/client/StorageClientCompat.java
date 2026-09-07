package com.sean.backpackininventory.client;

import com.sean.backpackininventory.compat.storage.StorageBackpackScreen;
import com.sean.backpackininventory.init.ModMenus;
import com.sean.backpackininventory.network.OpenStoragePayload;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;

final class StorageClientCompat {
    static void register(RegisterMenuScreensEvent event) { event.register(ModMenus.STORAGE_BACKPACK.get(), StorageBackpackScreen::new); }
    static void request(Screen screen) {
        if (screen != null && screen.getClass() == StorageScreen.class && ClientPreferences.CONTAINERS.get()
                && ClientPreferences.OPENING_MODE.get() != ClientPreferences.OpeningMode.VANILLA) {
            var storage = (StorageScreen) screen;
            PacketDistributor.sendToServer(new OpenStoragePayload(storage.getMenu().containerId,
                    ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.EQUIPPED_ONLY));
        }
    }
}
