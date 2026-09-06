package com.sean.backpackininventory.init;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.data.SelectedBackpackData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, BackpackInInventory.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SelectedBackpackData>> SELECTED_BACKPACK =
            REGISTER.register("selected_backpack", () -> AttachmentType.serializable(SelectedBackpackData::new).copyOnDeath().build());

    private ModAttachments() {
    }
}
