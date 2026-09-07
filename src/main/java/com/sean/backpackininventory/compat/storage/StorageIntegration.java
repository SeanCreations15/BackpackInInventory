package com.sean.backpackininventory.compat.storage;

import com.sean.backpackininventory.init.ModAttachments;
import com.sean.backpackininventory.menu.BackpackLocator;
import com.sean.backpackininventory.menu.CompanionBackpackData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedstorage.common.gui.StorageContainerMenu;

public final class StorageIntegration {
    private StorageIntegration() { }
    public static void open(ServerPlayer player, int expectedMenu, boolean equippedOnly) {
        // Use the already-authorized, still-open chest. No client-supplied block position.
        if (player.containerMenu.containerId != expectedMenu || player.isSpectator()
                || !(player.containerMenu instanceof StorageContainerMenu source)
                || source.getClass() != StorageContainerMenu.class || !source.stillValid(player)
                || !source.getCarried().isEmpty()) return;
        var found = BackpackLocator.find(player).stream().filter(b -> !equippedOnly || b.worn()).toList();
        var selected = BackpackLocator.select(found, player.getData(ModAttachments.SELECTED_BACKPACK.get()).selected());
        if (selected.isEmpty() || source.getBlockPosition().isEmpty()) return;
        var backpack = selected.get();
        var pos = source.getBlockPosition().orElseThrow();
        int count = backpack.context().getBackpackWrapper(player).getInventoryHandler().getSlots();
        var wrapper = backpack.context().getBackpackWrapper(player);
        int rows = Math.max(1, wrapper.getNumberOfSlotRows());
        int columns = CompanionBackpackData.columnsFor(count, rows);
        if (count < 1 || count > 4096) return;
        player.openMenu(new SophisticatedMenuProvider(
                (id, inventory, owner) -> StorageBackpackMenu.create(id, owner, pos, backpack),
                source.getStorageBlockEntity().getDisplayName(), false), buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeUUID(backpack.uuid());
                    buffer.writeVarInt(count);
                    buffer.writeVarInt(columns);
                    ItemStack.STREAM_CODEC.encode(buffer, backpack.stack());
                });
    }
}
