package com.sean.backpackininventory.compat.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StorageMenuSlotAccountingTest {
    @Test
    void acceptsLargeDoubleChestWithFullBackpack() {
        assertFalse(StorageMenuSlotAccounting.isInvalid(false, 216 + 36 + 120, 216, 120));
    }

    @Test
    void rejectsMissingOrUnexpectedSlotsOnServer() {
        assertTrue(StorageMenuSlotAccounting.isInvalid(false, 216 + 36 + 119, 216, 120));
        assertTrue(StorageMenuSlotAccounting.isInvalid(false, 216 + 36 + 121, 216, 120));
    }

    @Test
    void leavesClientValidationToTheServer() {
        assertFalse(StorageMenuSlotAccounting.isInvalid(true, 0, 216, 120));
    }
}
