package com.sean.backpackininventory.client;

import java.util.List;
import net.blay09.mods.trashslot.api.IGuiContainerLayout;
import net.blay09.mods.trashslot.api.SlotRenderStyle;
import net.blay09.mods.trashslot.api.Snap;
import net.blay09.mods.trashslot.api.TrashSlotAPI;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.neoforged.fml.ModList;

/** Keeps TrashSlot attached to the unchanged vanilla inventory panel. */
final class TrashSlotClientCompat {
    private TrashSlotClientCompat() {
    }

    static void register() {
        if (ModList.get().isLoaded("trashslot")) {
            IntegratedTrashSlotLayout.register();
        }
    }
}

// Kept separate so a client without the optional TrashSlot mod never loads a class
// whose signatures refer to the TrashSlot API.
final class IntegratedTrashSlotLayout implements IGuiContainerLayout {
        private static final int SLOT_SIZE = SlotRenderStyle.LONE.getWidth();
        private static final String VANILLA_INVENTORY_ID = InventoryScreen.class.getName().replace('.', '/');

        private IntegratedTrashSlotLayout() {
        }

        static void register() {
            TrashSlotAPI.registerLayout(IntegratedBackpackScreen.class, new IntegratedTrashSlotLayout());
        }

        @Override
        public List<Rect2i> getCollisionAreas(AbstractContainerScreen<?> container) {
            IntegratedBackpackScreen screen = screen(container);
            int offsetX = screen.trashSlotOffsetX();
            int offsetY = screen.trashSlotOffsetY();
            return List.of(new Rect2i(
                    screen.playerPanelLeft() - offsetX,
                    screen.playerPanelTop() - offsetY,
                    IntegratedBackpackScreen.VANILLA_WIDTH,
                    IntegratedBackpackScreen.VANILLA_HEIGHT));
        }

        @Override
        public List<Snap> getSnaps(AbstractContainerScreen<?> container, SlotRenderStyle style) {
            IntegratedBackpackScreen screen = screen(container);
            int offsetX = screen.trashSlotOffsetX();
            int offsetY = screen.trashSlotOffsetY();
            int left = screen.playerPanelLeft() - offsetX;
            int top = screen.playerPanelTop() - offsetY;
            int right = left + IntegratedBackpackScreen.VANILLA_WIDTH - style.getWidth();
            int bottom = top + IntegratedBackpackScreen.VANILLA_HEIGHT - style.getHeight();
            return List.of(
                    new Snap(Snap.Type.HORIZONTAL, 0, top),
                    new Snap(Snap.Type.HORIZONTAL, 0, bottom),
                    new Snap(Snap.Type.VERTICAL, left, 0),
                    new Snap(Snap.Type.VERTICAL, right, 0));
        }

        @Override
        public SlotRenderStyle getSlotRenderStyle(AbstractContainerScreen<?> container, int x, int y) {
            IntegratedBackpackScreen screen = screen(container);
            int actualX = x + screen.trashSlotOffsetX();
            int actualY = y + screen.trashSlotOffsetY();
            int left = screen.playerPanelLeft();
            int top = screen.playerPanelTop();
            int right = left + IntegratedBackpackScreen.VANILLA_WIDTH;
            int bottom = top + IntegratedBackpackScreen.VANILLA_HEIGHT;

            if (actualY == bottom) {
                if (actualX == left) {
                    return SlotRenderStyle.ATTACH_BOTTOM_LEFT;
                }
                if (actualX + SLOT_SIZE == right) {
                    return SlotRenderStyle.ATTACH_BOTTOM_RIGHT;
                }
                if (actualX >= left && actualX + SLOT_SIZE <= right) {
                    return SlotRenderStyle.ATTACH_BOTTOM_CENTER;
                }
            }
            if (actualY + SLOT_SIZE == top) {
                if (actualX == left) {
                    return SlotRenderStyle.ATTACH_TOP_LEFT;
                }
                if (actualX + SLOT_SIZE == right) {
                    return SlotRenderStyle.ATTACH_TOP_RIGHT;
                }
                if (actualX >= left && actualX + SLOT_SIZE <= right) {
                    return SlotRenderStyle.ATTACH_TOP_CENTER;
                }
            }
            if (actualX + SLOT_SIZE == left) {
                if (actualY == top) {
                    return SlotRenderStyle.ATTACH_LEFT_TOP;
                }
                if (actualY + SLOT_SIZE == bottom) {
                    return SlotRenderStyle.ATTACH_LEFT_BOTTOM;
                }
                if (actualY >= top && actualY + SLOT_SIZE <= bottom) {
                    return SlotRenderStyle.ATTACH_LEFT_CENTER;
                }
            }
            if (actualX == right) {
                if (actualY == top) {
                    return SlotRenderStyle.ATTACH_RIGHT_TOP;
                }
                if (actualY + SLOT_SIZE == bottom) {
                    return SlotRenderStyle.ATTACH_RIGHT_BOTTOM;
                }
                if (actualY >= top && actualY + SLOT_SIZE <= bottom) {
                    return SlotRenderStyle.ATTACH_RIGHT_CENTER;
                }
            }
            return SlotRenderStyle.LONE;
        }

        @Override
        public int getDefaultSlotX(AbstractContainerScreen<?> container) {
            return IntegratedBackpackScreen.VANILLA_WIDTH / 2 - SLOT_SIZE;
        }

        @Override
        public int getDefaultSlotY(AbstractContainerScreen<?> container) {
            return IntegratedBackpackScreen.VANILLA_HEIGHT / 2;
        }

        @Override
        public boolean isEnabledByDefault() {
            return true;
        }

        @Override
        public int getSlotOffsetX(AbstractContainerScreen<?> container, SlotRenderStyle style) {
            return screen(container).trashSlotOffsetX();
        }

        @Override
        public int getSlotOffsetY(AbstractContainerScreen<?> container, SlotRenderStyle style) {
            return screen(container).trashSlotOffsetY();
        }

        @Override
        public String getContainerId(AbstractContainerScreen<?> container) {
            // Share visibility, locking and position with the normal survival inventory.
            return VANILLA_INVENTORY_ID;
        }

        private static IntegratedBackpackScreen screen(AbstractContainerScreen<?> container) {
            return (IntegratedBackpackScreen) container;
        }
}
