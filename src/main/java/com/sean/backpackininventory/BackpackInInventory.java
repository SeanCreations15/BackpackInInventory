package com.sean.backpackininventory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import com.sean.backpackininventory.client.ClientSetup;
import com.sean.backpackininventory.init.ModAttachments;
import com.sean.backpackininventory.init.ModMenus;
import com.sean.backpackininventory.network.ModPayloads;

@Mod(BackpackInInventory.MOD_ID)
public final class BackpackInInventory {
    public static final String MOD_ID = "backpackininventory";

    public BackpackInInventory(IEventBus modBus, Dist dist, net.neoforged.fml.ModContainer container) {
        ModMenus.REGISTER.register(modBus);
        ModAttachments.REGISTER.register(modBus);
        modBus.addListener(ModPayloads::register);
        if (dist == Dist.CLIENT) {
            container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT,
                    com.sean.backpackininventory.client.ClientPreferences.SPEC);
            ClientSetup.register(modBus);
        }
    }
}
