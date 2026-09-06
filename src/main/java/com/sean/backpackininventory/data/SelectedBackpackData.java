package com.sean.backpackininventory.data;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public final class SelectedBackpackData implements INBTSerializable<CompoundTag> {
    private static final String SELECTED = "selected";
    private UUID selected;

    public Optional<UUID> selected() {
        return Optional.ofNullable(selected);
    }

    public void setSelected(UUID selected) {
        this.selected = selected;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (selected != null) {
            tag.putUUID(SELECTED, selected);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        selected = tag.hasUUID(SELECTED) ? tag.getUUID(SELECTED) : null;
    }
}
