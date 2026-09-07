package com.sean.backpackininventory.client;

import com.sean.backpackininventory.menu.CompanionBackpackData;
import com.sean.backpackininventory.menu.CompanionBackpackMenus;
import com.sean.backpackininventory.network.AttachContainerPayload;
import com.sean.backpackininventory.network.OpenContainerPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ContainerCompanionClient {
    private static final int GAP = 6;

    private ContainerCompanionClient() { }

    public static void request(net.minecraft.client.gui.screens.Screen candidate) {
        if (!(candidate instanceof AbstractContainerScreen<?> screen)
                || candidate instanceof InventoryScreen
                || isNativeSophisticatedStorageScreen(candidate)
                || candidate.getClass().getName().equals(
                        "com.sean.backpackininventory.compat.storage.StorageBackpackScreen")
                || !CompanionBackpackMenus.isEligible(screen.getMenu())
                || !ClientPreferences.CONTAINERS.get()
                || ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.VANILLA) return;
        PacketDistributor.sendToServer(new OpenContainerPayload(screen.getMenu().containerId,
                ClientPreferences.OPENING_MODE.get() == ClientPreferences.OpeningMode.EQUIPPED_ONLY));
    }

    private static boolean isNativeSophisticatedStorageScreen(net.minecraft.client.gui.screens.Screen screen) {
        return net.neoforged.fml.ModList.get().isLoaded("sophisticatedstorage")
                && screen.getClass().getName().equals(
                        "net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen");
    }

    public static void attach(AttachContainerPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.containerMenu.containerId != payload.menuId()
                || !(minecraft.screen instanceof AbstractContainerScreen<?> screen)
                || screen.getMenu() != minecraft.player.containerMenu) return;
        CompanionBackpackMenus.attachClient(screen.getMenu(), payload.uuid(), payload.count(),
                payload.columns(), payload.icon()).ifPresent(data -> position(screen, data));
    }

    public static void renderBackground(ContainerScreenEvent.Render.Background event) {
        var screen = event.getContainerScreen();
        CompanionBackpackMenus.get(screen.getMenu()).ifPresent(data -> {
            position(screen, data);
            renderPanel(event.getGuiGraphics(), screen, data);
        });
    }

    private static void position(AbstractContainerScreen<?> screen, CompanionBackpackData data) {
        int panelX = screen.getGuiLeft() - GAP - panelWidth(data);
        int panelY = screen.getGuiTop();
        for (int i = 0; i < data.count(); i++) {
            Slot slot = screen.getMenu().getSlot(data.start() + i);
            slot.x = panelX - screen.getGuiLeft() + 8 + i % data.columns() * 18;
            slot.y = panelY - screen.getGuiTop() + 20 + i / data.columns() * 18;
        }
    }

    private static void renderPanel(GuiGraphics graphics, AbstractContainerScreen<?> screen,
            CompanionBackpackData data) {
        int panelX = screen.getGuiLeft() - GAP - panelWidth(data);
        int panelY = screen.getGuiTop();
        int width = panelWidth(data);
        int height = 28 + data.rows() * 18;
        graphics.fill(panelX - 1, panelY - 1, panelX + width + 1, panelY + height + 1, 0xff151515);
        graphics.fill(panelX, panelY, panelX + width, panelY + height, 0xffc6c6c6);
        graphics.drawString(Minecraft.getInstance().font,
                Minecraft.getInstance().font.plainSubstrByWidth(data.icon().getHoverName().getString(), width - 34),
                panelX + 28, panelY + 6, 0xff404040, false);
        graphics.renderItem(data.icon(), panelX + 7, panelY + 2);
        for (int i = 0; i < data.count(); i++) {
            int x = panelX + 7 + i % data.columns() * 18;
            int y = panelY + 19 + i / data.columns() * 18;
            graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 7, 83, 18, 18);
        }
    }

    private static int panelWidth(CompanionBackpackData data) {
        return 14 + data.columns() * 18;
    }
}
