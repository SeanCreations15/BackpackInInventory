package com.sean.backpackininventory.mixin;

import com.sean.backpackininventory.menu.CompanionBackpackMenus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void backpackininventory$handleCompanionClick(int slotId, int button,
            ClickType clickType, Player player, CallbackInfo callback) {
        if (CompanionBackpackMenus.handleClick(
                (AbstractContainerMenu) (Object) this, slotId, button, clickType, player)) {
            callback.cancel();
        }
    }
}
