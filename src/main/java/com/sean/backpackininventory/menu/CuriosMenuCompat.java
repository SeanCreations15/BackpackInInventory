package com.sean.backpackininventory.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.common.inventory.CurioSlot;

/** Optional Curios integration without making Curios a hard dependency. */
final class CuriosMenuCompat {
    private CuriosMenuCompat() {
    }

    static int addSlots(IntegratedBackpackMenu menu, Player player) {
        return ModList.get().isLoaded("curios") ? CuriosMenuIntegration.addSlots(menu, player) : 0;
    }
}

// This class is only resolved when Curios is installed.
final class CuriosMenuIntegration {
    private CuriosMenuIntegration() {
    }

    static int addSlots(IntegratedBackpackMenu menu, Player player) {
        var curios = CuriosApi.getCuriosInventory(player).orElse(null);
        if (curios == null) {
            return 0;
        }

        int added = 0;
        for (var entry : curios.getCurios().entrySet()) {
            String identifier = entry.getKey();
            var handler = entry.getValue();
            if (!handler.isVisible()) {
                continue;
            }
            var stacks = handler.getStacks();
            for (int slotIndex = 0; slotIndex < stacks.getSlots(); slotIndex++) {
                Slot slot = new IntegratedCurioSlot(menu, player, stacks, slotIndex, identifier,
                        handler.getRenders(), handler.getActiveStates(), handler.canToggleRendering());
                menu.addIntegratedExtraSlot(slot);
                added++;
            }
        }
        return added;
    }

    private static final class IntegratedCurioSlot extends CurioSlot {
        private final IntegratedBackpackMenu menu;

        private IntegratedCurioSlot(IntegratedBackpackMenu menu, Player player,
                top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler handler,
                int slotIndex, String identifier, net.minecraft.core.NonNullList<Boolean> renders,
                java.util.List<Boolean> activeStates, boolean canToggleRender) {
            super(player, handler, slotIndex, identifier, -2_000, -2_000, renders, activeStates,
                    canToggleRender, false, false);
            this.menu = menu;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !isBackingBackpack(getItem()) && super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !isBackingBackpack(getItem()) && super.mayPlace(stack);
        }

        private boolean isBackingBackpack(ItemStack stack) {
            return menu.isSelectedBackpack(stack);
        }
    }
}
