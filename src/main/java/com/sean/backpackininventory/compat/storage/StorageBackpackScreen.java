package com.sean.backpackininventory.compat.storage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;

/** Native chest controls and scrolling, with an independently paged backpack. */
public final class StorageBackpackScreen extends StorageScreen {
    private int page;
    private int rows;
    private int panelX;
    private int panelY;
    private int virtualWidth;
    private Button previous;
    private Button next;

    public StorageBackpackScreen(net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
    private StorageBackpackMenu combined() { return (StorageBackpackMenu) getMenu(); }
    private int capacity() { return rows * 9; }
    private int pages() { return Math.max(1, (combined().backpackCount() + capacity() - 1) / capacity()); }

    @Override protected void init() {
        int actualWidth = width;
        rows = Math.max(1, Math.min(8, (height - 60) / 18));
        virtualWidth = actualWidth + 184;
        width = virtualWidth;
        try { super.init(); } finally { width = actualWidth; }
        panelX = leftPos - 184;
        panelY = Math.max(20, (height - (rows * 18 + 32)) / 2);
        previous = addRenderableWidget(Button.builder(Component.literal("<"), b -> turnPage(-1)).bounds(panelX, panelY - 18, 18, 16).build());
        next = addRenderableWidget(Button.builder(Component.literal(">"), b -> turnPage(1)).bounds(panelX + 158, panelY - 18, 18, 16).build());
        positionBackpack();
    }
    private void turnPage(int delta) { page = Math.floorMod(page + delta, pages()); positionBackpack(); }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x >= panelX && x < panelX + 176 && y >= panelY && y < panelY + rows * 18 + 28) {
            if (vertical != 0) turnPage(vertical > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override protected boolean hasClickedOutside(double x, double y, int left, int top, int button) {
        if (x >= panelX && x < panelX + 176 && y >= panelY - 18 && y < panelY + rows * 18 + 28) return false;
        return super.hasClickedOutside(x, y, left, top, button);
    }

    @Override protected void slotClicked(Slot slot, int id, int button, net.minecraft.world.inventory.ClickType type) {
        if (slot != null && combined().isBacking(slot.getItem())) {
            minecraft.player.closeContainer();
            com.sean.backpackininventory.client.ClientInventoryInterceptor.openVanilla();
            return;
        }
        super.slotClicked(slot, id, button, type);
    }

    @Override protected void updateExtraSlotsPositions() {
        // Called by upstream during initialization; absolute anchors are set afterwards.
        if (rows > 0) positionBackpack();
    }
    private void positionBackpack() {
        page = Math.max(0, Math.min(page, pages() - 1));
        int first = page * capacity();
        for (int i = 0; i < combined().backpackCount(); i++) {
            Slot slot = getMenu().getSlot(combined().backpackStart() + i);
            int offset = i - first;
            slot.x = offset >= 0 && offset < capacity() ? panelX - leftPos + 8 + offset % 9 * 18 : -2000;
            slot.y = offset >= 0 && offset < capacity() ? panelY - topPos + 20 + offset / 9 * 18 : -2000;
        }
        if (previous != null) previous.visible = next.visible = pages() > 1;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        int actualWidth = width;
        width = virtualWidth;
        try { super.renderBg(graphics, partial, mouseX, mouseY); } finally { width = actualWidth; }
        int panelHeight = rows * 18 + 28;
        graphics.fill(panelX - 1, panelY - 1, panelX + 177, panelY + panelHeight + 1, 0xff151515);
        graphics.fill(panelX, panelY, panelX + 176, panelY + panelHeight, 0xffc6c6c6);
        graphics.drawString(font, font.plainSubstrByWidth(combined().backpackIcon().getHoverName().getString(), 160), panelX + 8, panelY + 6, 0xff404040, false);
        for (int i = 0; i < Math.min(capacity(), combined().backpackCount() - page * capacity()); i++) {
            int x = panelX + 7 + i % 9 * 18, y = panelY + 19 + i / 9 * 18;
            graphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, x, y, 7, 83, 18, 18);
        }
        if (pages() > 1) graphics.drawCenteredString(font, (page + 1) + " / " + pages(), panelX + 88, panelY - 14, 0xffffffff);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        super.render(graphics, mouseX, mouseY, partial);
        // StorageScreenBase draws only the final 36 non-storage slots. Repair the
        // prefix excluded by the additional backpack slots, just as in the E screen.
        int storageStart = getMenu().getNumberOfStorageInventorySlots();
        int skippedPlayer = Math.min(36, combined().backpackCount());
        int skippedBackpack = Math.max(0, combined().backpackCount() - 36);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        boolean hovered = false;
        for (int i = 0; i < skippedPlayer; i++) {
            Slot slot = getMenu().getSlot(storageStart + i);
            if (!slot.isActive() || slot.x < -1000) continue;
            renderSlot(graphics, slot);
            if (isHovering(slot, mouseX, mouseY)) { hoveredSlot = slot; hovered = true; renderSlotHighlight(graphics, slot.x, slot.y, 0, getSlotColor(slot.index)); }
        }
        for (int i = 0; i < skippedBackpack; i++) {
            Slot slot = getMenu().getSlot(combined().backpackStart() + i);
            if (!slot.isActive() || slot.x < -1000) continue;
            renderSlot(graphics, slot);
            if (isHovering(slot, mouseX, mouseY)) { hoveredSlot = slot; hovered = true; renderSlotHighlight(graphics, slot.x, slot.y, 0, getSlotColor(slot.index)); }
        }
        graphics.pose().popPose();
        if (hovered) renderTooltip(graphics, mouseX, mouseY);
    }
}
