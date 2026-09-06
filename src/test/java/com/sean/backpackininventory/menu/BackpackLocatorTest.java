package com.sean.backpackininventory.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class BackpackLocatorTest {
    @Test
    void rememberedBackpackWinsWhenAccessible() {
        LocatedBackpack first = backpack(1);
        LocatedBackpack remembered = backpack(2);

        assertEquals(remembered.uuid(),
                BackpackLocator.select(List.of(first, remembered), Optional.of(remembered.uuid())).orElseThrow().uuid());
    }

    @Test
    void stalePreferenceFallsBackToFirstAccessibleBackpack() {
        LocatedBackpack first = backpack(1);

        assertEquals(first.uuid(),
                BackpackLocator.select(List.of(first), Optional.of(UUID.randomUUID())).orElseThrow().uuid());
    }

    @Test
    void emptyInventoryHasNoSelection() {
        assertEquals(Optional.empty(), BackpackLocator.select(List.of(), Optional.empty()));
    }

    private static LocatedBackpack backpack(int priority) {
        return new LocatedBackpack(UUID.randomUUID(), ItemStack.EMPTY, null, false, priority);
    }
}
