package com.sean.backpackininventory.client;

import com.sean.backpackininventory.network.OpenInventoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackSettingsScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;

public final class ClientInventoryInterceptor {
    private static boolean allowNextVanilla;

    private ClientInventoryInterceptor() {
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (net.neoforged.fml.ModList.get().isLoaded("sophisticatedstorage")) {
            StorageClientCompat.request(event.getNewScreen());
        }
        ContainerCompanionClient.request(event.getNewScreen());
        if (replaceIntegratedSettingsScreen(event)) {
            return;
        }
        if (!(event.getNewScreen() instanceof InventoryScreen)) {
            return;
        }
        if (allowNextVanilla) {
            allowNextVanilla = false;
            return;
        }
        if (event.getCurrentScreen() != null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.isCreative() || minecraft.player.isSpectator()
                || ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.VANILLA || !hasBackpack()) {
            return;
        }

        event.setNewScreen(null);
        PacketDistributor.sendToServer(new OpenInventoryPayload(ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.EQUIPPED_ONLY));
    }

    private static boolean replaceIntegratedSettingsScreen(ScreenEvent.Opening event) {
        if (!(event.getCurrentScreen() instanceof IntegratedBackpackScreen)
                || !(event.getNewScreen() instanceof BackpackSettingsScreen settingsScreen)
                || !(settingsScreen.getMenu() instanceof SettingsContainerMenu<?> settingsMenu)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        event.setNewScreen(new IntegratedBackpackSettingsScreen(
                settingsMenu, minecraft.player.getInventory(), settingsScreen.getTitle()));
        return true;
    }

    private static boolean hasBackpack() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean[] found = {false};
        PlayerInventoryProvider.get().runOnBackpacks(minecraft.player, (stack, handler, identifier, slot) -> {
            if (ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.EQUIPPED_ONLY
                    && (handler.equals(PlayerInventoryProvider.MAIN_INVENTORY) || handler.equals(PlayerInventoryProvider.OFFHAND_INVENTORY))) return false;
            found[0] = true;
            return true;
        });
        return found[0];
    }

    public static void openVanilla() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            allowNextVanilla = true;
            minecraft.setScreen(new InventoryScreen(minecraft.player));
        }
    }

    public static void allowNextVanillaOpen() {
        allowNextVanilla = true;
    }
}
