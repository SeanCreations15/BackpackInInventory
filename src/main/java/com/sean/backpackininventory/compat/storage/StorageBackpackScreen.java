package com.sean.backpackininventory.compat.storage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;

/** Native Sophisticated Storage controls with a full-size backpack beside them. */
public final class StorageBackpackScreen extends StorageScreen {
    private static final int GAP = 6;
    private int panelX;
    private int panelY;

    public StorageBackpackScreen(net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu menu,
            Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private StorageBackpackMenu combined() { return (StorageBackpackMenu) getMenu(); }
    private int columns() { return combined().backpackColumns(); }
    private int rows() { return (combined().backpackCount() + columns() - 1) / columns(); }
    private int panelWidth() { return 14 + columns() * 18; }

    @Override protected void init() {
        super.init();
        positionBackpack();
    }

    @Override protected boolean hasClickedOutside(double x, double y, int left, int top, int button) {
        if (x >= panelX && x < panelX + panelWidth()
                && y >= panelY && y < panelY + rows() * 18 + 28) return false;
        return super.hasClickedOutside(x, y, left, top, button);
    }

    @Override protected void slotClicked(Slot slot, int id, int button,
            net.minecraft.world.inventory.ClickType type) {
        if (slot != null && combined().isBacking(slot.getItem())) {
            minecraft.player.closeContainer();
            com.sean.backpackininventory.client.ClientInventoryInterceptor.openVanilla();
            return;
        }
        super.slotClicked(slot, id, button, type);
    }

    @Override protected void updateExtraSlotsPositions() {
        if (columns() > 0) positionBackpack();
    }

    @Override protected void updatePlayerSlotsPositions() {
        int start = getMenu().getNumberOfStorageInventorySlots();
        int x = storageBackgroundProperties.getPlayerInventoryXOffset() + 8;
        int y = inventoryLabelY + 12;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                Slot slot = getMenu().getSlot(start + row * 9 + column);
                slot.x = x + column * 18;
                slot.y = y + row * 18;
            }
        }
        y += 58;
        for (int column = 0; column < 9; column++) {
            Slot slot = getMenu().getSlot(start + 27 + column);
            slot.x = x + column * 18;
            slot.y = y;
        }
    }

    private void positionBackpack() {
        panelX = leftPos - GAP - panelWidth();
        panelY = topPos;
        for (int i = 0; i < combined().backpackCount(); i++) {
            Slot slot = getMenu().getSlot(combined().backpackStart() + i);
            slot.x = panelX - leftPos + 8 + i % columns() * 18;
            slot.y = panelY - topPos + 20 + i / columns() * 18;
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        super.renderBg(graphics, partial, mouseX, mouseY);
        positionBackpack();
        for (int i = 0; i < getMenu().getNumberOfStorageInventorySlots(); i++) {
            Slot slot = getMenu().getSlot(i);
            if (slot.isActive() && slot.x > -1000) {
                graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION,
                        leftPos + slot.x - 1, topPos + slot.y - 1, 7, 83, 18, 18);
            }
        }
        int height = rows() * 18 + 28;
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth() + 1, panelY + height + 1, 0xff151515);
        graphics.fill(panelX, panelY, panelX + panelWidth(), panelY + height, 0xffc6c6c6);
        graphics.renderItem(combined().backpackIcon(), panelX + 7, panelY + 2);
        graphics.drawString(font,
                font.plainSubstrByWidth(combined().backpackIcon().getHoverName().getString(), panelWidth() - 34),
                panelX + 28, panelY + 6, 0xff404040, false);
        for (int i = 0; i < combined().backpackCount(); i++) {
            int x = panelX + 7 + i % columns() * 18;
            int y = panelY + 19 + i / columns() * 18;
            graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 7, 83, 18, 18);
        }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        int storageStart = getMenu().getNumberOfStorageInventorySlots();
        int skippedBackpack = Math.max(0, combined().backpackCount() - 36);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        boolean hovered = false;
        for (int i = 0; i < 36; i++) {
            Slot slot = getMenu().getSlot(storageStart + i);
            if (!slot.isActive() || slot.x < -1000) continue;
            renderSlot(graphics, slot);
            if (isHovering(slot, mouseX, mouseY)) {
                hoveredSlot = slot;
                hovered = true;
                renderSlotHighlight(graphics, slot.x, slot.y, 0, getSlotColor(slot.index));
            }
        }
        for (int i = 0; i < skippedBackpack; i++) {
            Slot slot = getMenu().getSlot(combined().backpackStart() + i);
            if (!slot.isActive() || slot.x < -1000) continue;
            renderSlot(graphics, slot);
            if (isHovering(slot, mouseX, mouseY)) {
                hoveredSlot = slot;
                hovered = true;
                renderSlotHighlight(graphics, slot.x, slot.y, 0, getSlotColor(slot.index));
            }
        }
        graphics.pose().popPose();
        if (hovered) renderTooltip(graphics, mouseX, mouseY);
    }
}
