package com.sean.backpackininventory.client;

import java.util.Optional;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.gui.CuriosButton;
import top.theillusivec4.curios.client.gui.CuriosScreen;
import top.theillusivec4.curios.common.network.client.CPacketOpenCurios;

/** Loaded only when Curios is present. */
final class CuriosClientCompat {
    private CuriosClientCompat() {
    }

    static Optional<ImageButton> createInventoryButton(AbstractContainerScreen<?> parent, int inventoryLeft,
            int inventoryTop, Runnable toggleDrawer) {
        if (!CuriosClientConfig.CLIENT.enableButton.get()) {
            return Optional.empty();
        }

        var offset = CuriosScreen.getButtonOffset(false);
        ImageButton button = new ImageButton(
                inventoryLeft + offset.getA() + 2,
                inventoryTop + offset.getB() + 85,
                10,
                10,
                CuriosButton.BIG,
                ignored -> toggleDrawer.run());
        button.setTooltip(Tooltip.create(Component.translatable("curios.name")));
        return Optional.of(button);
    }

    static void openManagementScreen(ItemStack carried) {
        // The selected backpack is intentionally locked while it backs the integrated
        // storage menu. Once Curios safely closes that menu, give the player one plain
        // inventory opening in which the newly unequipped backpack can be moved.
        ClientInventoryInterceptor.allowNextVanillaOpen();
        PacketDistributor.sendToServer(new CPacketOpenCurios(carried.copy()));
    }
}
