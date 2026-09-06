package com.sean.backpackininventory.menu;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public record BackpackDescriptor(UUID uuid, ItemStack displayStack, boolean worn) {
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        ItemStack.STREAM_CODEC.encode(buffer, displayStack);
        buffer.writeBoolean(worn);
    }

    public static BackpackDescriptor read(RegistryFriendlyByteBuf buffer) {
        return new BackpackDescriptor(buffer.readUUID(), ItemStack.STREAM_CODEC.decode(buffer), buffer.readBoolean());
    }
}
