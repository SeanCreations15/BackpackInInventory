package com.sean.backpackininventory.client;

import com.sean.backpackininventory.init.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientSetup {
    private ClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ClientSetup::registerScreens);
        NeoForge.EVENT_BUS.addListener(ClientInventoryInterceptor::onScreenOpening);
        NeoForge.EVENT_BUS.addListener(ContainerCompanionClient::renderBackground);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.INTEGRATED_BACKPACK.get(), IntegratedBackpackScreen::new);
        if (net.neoforged.fml.ModList.get().isLoaded("sophisticatedstorage")) {
            StorageClientCompat.register(event);
        }
        TrashSlotClientCompat.register();
    }
}
