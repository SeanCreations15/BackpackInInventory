package com.sean.backpackininventory.mixin;

import java.util.List;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StorageContainerMenuBase.class)
public interface StorageContainerMenuBaseAccessor {
    @Mutable
    @Accessor("extraSlots")
    void backpackininventory$setExtraSlots(List<Slot> slots);

    @Invoker("addExtraSlot")
    void backpackininventory$addExtraSlot(Slot slot);
}
