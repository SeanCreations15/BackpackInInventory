package com.sean.backpackininventory.compat.storage;

final class StorageMenuSlotAccounting {
    private static final int PLAYER_SLOTS = 36;

    private StorageMenuSlotAccounting() { }

    static boolean isInvalid(boolean clientSide, int actualSlots, int storageSlots, int backpackSlots) {
        return !clientSide && actualSlots != storageSlots + PLAYER_SLOTS + backpackSlots;
    }
}
