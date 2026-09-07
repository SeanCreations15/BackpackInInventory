package com.sean.backpackininventory.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CompanionBackpackDataTest {
    @Test
    void preservesNineColumnBackpackShape() {
        assertEquals(9, CompanionBackpackData.columnsFor(54, 6));
        assertEquals(9, CompanionBackpackData.columnsFor(81, 9));
    }

    @Test
    void preservesTwelveColumnBackpackShape() {
        assertEquals(12, CompanionBackpackData.columnsFor(108, 9));
    }

    @Test
    void preservesWidthAfterColumnsAreConsumedByUpgrades() {
        assertEquals(11, CompanionBackpackData.columnsFor(99, 9));
        assertEquals(8, CompanionBackpackData.columnsFor(72, 9));
    }
}
