package com.sean.backpackininventory.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DrawerLayoutTest {
    @Test void resizingFromLastPageNeverLosesAllSlots() {
        var narrow = DrawerLayout.calculate(61, 53, 7);
        var wide = DrawerLayout.calculate(61, 116, narrow.page());
        assertTrue(wide.count() > 0);
        assertTrue(wide.first() < 61);
        assertEquals(wide.pages() - 1, wide.page());
    }
    @Test void allSlotsAreReachableExactlyOnceAcrossPages() {
        for (int slots : new int[]{0, 1, 8, 19, 40, 61, 144, 4096}) {
            for (int gap : new int[]{53, 71, 116, 160}) {
                int visited = 0;
                int pages = DrawerLayout.calculate(slots, gap, 0).pages();
                for (int page = 0; page < pages; page++) {
                    var layout = DrawerLayout.calculate(slots, gap, page);
                    assertEquals(visited, layout.first());
                    assertTrue(layout.count() <= 8 * layout.columns());
                    assertTrue(7 + 18 * layout.columns() + 28 <= gap);
                    visited += layout.count();
                }
                assertEquals(slots, visited);
            }
        }
    }
    @Test void invalidPagePreferencesClamp() {
        assertEquals(0, DrawerLayout.calculate(19, 116, -5).page());
        assertEquals(0, DrawerLayout.calculate(0, 116, 900).page());
    }
}
