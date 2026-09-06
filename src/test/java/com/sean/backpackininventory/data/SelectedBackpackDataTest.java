package com.sean.backpackininventory.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SelectedBackpackDataTest {
    @Test
    void selectedUuidRoundTripsThroughNbt() {
        UUID selected = UUID.randomUUID();
        SelectedBackpackData original = new SelectedBackpackData();
        original.setSelected(selected);

        SelectedBackpackData restored = new SelectedBackpackData();
        restored.deserializeNBT(null, original.serializeNBT(null));

        assertEquals(selected, restored.selected().orElseThrow());
    }
}
