package com.sean.backpackininventory.init;

import com.sean.backpackininventory.BackpackInInventory;
import com.sean.backpackininventory.menu.IntegratedBackpackMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, BackpackInInventory.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<IntegratedBackpackMenu>> INTEGRATED_BACKPACK =
            REGISTER.register("integrated_backpack", () -> IMenuTypeExtension.create(IntegratedBackpackMenu::fromBuffer));

    private ModMenus() {
    }
}
