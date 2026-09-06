package com.sean.backpackininventory.menu;

import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;

public record LocatedBackpack(UUID uuid, ItemStack stack, BackpackContext.Item context, boolean worn, int priority) {
    public BackpackDescriptor descriptor() {
        return new BackpackDescriptor(uuid, stack.copy(), worn);
    }
}
