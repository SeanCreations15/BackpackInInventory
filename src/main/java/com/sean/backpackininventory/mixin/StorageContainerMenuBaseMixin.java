package com.sean.backpackininventory.mixin;

import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageContainerMenuBase.class)
public abstract class StorageContainerMenuBaseMixin {
    @Shadow private int extraSlotsSize;

    @Inject(method = "refreshAllSlots", at = @At("HEAD"))
    private void backpackininventory$resetExtraSlotCount(CallbackInfo callback) {
        extraSlotsSize = 0;
    }
}
