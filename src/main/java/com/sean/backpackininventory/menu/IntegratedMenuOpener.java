package com.sean.backpackininventory.menu;

import com.sean.backpackininventory.init.ModAttachments;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;

public final class IntegratedMenuOpener {
    private IntegratedMenuOpener() {
    }

    public static boolean open(ServerPlayer player, Optional<UUID> requested) {
        return open(player, requested, false);
    }

    public static boolean open(ServerPlayer player, Optional<UUID> requested, boolean equippedOnly) {
        List<LocatedBackpack> found = BackpackLocator.find(player).stream().filter(b -> !equippedOnly || b.worn()).toList();
        Optional<LocatedBackpack> selected;
        if (requested.isPresent()) {
            selected = found.stream().filter(backpack -> backpack.uuid().equals(requested.get())).findFirst();
        } else {
            Optional<UUID> preference = player.getData(ModAttachments.SELECTED_BACKPACK.get()).selected();
            selected = BackpackLocator.select(found, preference);
        }
        if (selected.isEmpty()) {
            return false;
        }

        LocatedBackpack backpack = selected.get();
        player.getData(ModAttachments.SELECTED_BACKPACK.get()).setSelected(backpack.uuid());
        List<BackpackDescriptor> descriptors = found.stream().map(LocatedBackpack::descriptor).toList();
        player.openMenu(new SophisticatedMenuProvider(
                (id, inventory, owner) -> new IntegratedBackpackMenu(id, owner, backpack.context(), descriptors),
                backpack.stack().getHoverName(), false), buffer -> {
                    backpack.context().toBuffer(buffer);
                    buffer.writeVarInt(descriptors.size());
                    descriptors.forEach(descriptor -> descriptor.write(buffer));
                });
        return true;
    }
}
