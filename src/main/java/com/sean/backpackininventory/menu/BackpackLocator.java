package com.sean.backpackininventory.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;

public final class BackpackLocator {
    private BackpackLocator() {
    }

    public static List<LocatedBackpack> find(Player player) {
        Map<UUID, LocatedBackpack> byUuid = new LinkedHashMap<>();
        PlayerInventoryProvider.get().runOnBackpacks(player, (stack, handler, identifier, slot) -> {
            var wrapper = BackpackWrapper.fromStack(stack);
            wrapper.getInventoryHandler(); // Initializes the UUID using Sophisticated Backpacks' normal storage path.
            wrapper.getContentsUuid().ifPresent(uuid -> byUuid.putIfAbsent(uuid,
                    new LocatedBackpack(uuid, stack, new BackpackContext.Item(handler, identifier, slot, true), isWorn(handler), priority(handler))));
            return false;
        });
        List<LocatedBackpack> result = new ArrayList<>(byUuid.values());
        result.sort(Comparator.comparingInt(LocatedBackpack::priority));
        return List.copyOf(result);
    }

    public static Optional<LocatedBackpack> select(List<LocatedBackpack> backpacks, Optional<UUID> preferred) {
        if (preferred.isPresent()) {
            for (LocatedBackpack backpack : backpacks) {
                if (backpack.uuid().equals(preferred.get())) {
                    return Optional.of(backpack);
                }
            }
        }
        return backpacks.stream().findFirst();
    }

    private static boolean isWorn(String handler) {
        return !PlayerInventoryProvider.MAIN_INVENTORY.equals(handler)
                && !PlayerInventoryProvider.OFFHAND_INVENTORY.equals(handler);
    }

    private static int priority(String handler) {
        if (PlayerInventoryProvider.ARMOR_INVENTORY.equals(handler)) {
            return 1;
        }
        if (PlayerInventoryProvider.MAIN_INVENTORY.equals(handler)) {
            return 2;
        }
        if (PlayerInventoryProvider.OFFHAND_INVENTORY.equals(handler)) {
            return 3;
        }
        return 0; // Curios and other wearable integrations.
    }
}
