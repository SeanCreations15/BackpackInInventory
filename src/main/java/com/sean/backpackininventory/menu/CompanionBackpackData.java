package com.sean.backpackininventory.menu;

import java.util.UUID;
import net.minecraft.world.item.ItemStack;

public record CompanionBackpackData(UUID uuid, int start, int count, int columns, ItemStack icon) {
    public static int columnsFor(int count, int rows) {
        return Math.max(1, Math.min(12, (count + Math.max(1, rows) - 1) / Math.max(1, rows)));
    }

    public int rows() {
        return (count + columns - 1) / columns;
    }
}
