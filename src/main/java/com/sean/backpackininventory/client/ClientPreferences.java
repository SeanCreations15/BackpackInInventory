package com.sean.backpackininventory.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientPreferences {
    public enum OpeningMode { ANY_BACKPACK, EQUIPPED_ONLY, VANILLA }
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<OpeningMode> OPENING_MODE;
    public static final ModConfigSpec.BooleanValue CONTAINERS;
    public static int rememberedCuriosPage;
    static {
        var builder = new ModConfigSpec.Builder();
        OPENING_MODE = builder.comment("Automatically open any backpack, equipped backpacks only, or vanilla inventory.")
                .defineEnum("openingMode", OpeningMode.ANY_BACKPACK);
        CONTAINERS = builder.comment("Show the selected backpack beside opened containers.")
                .define("chestIntegration", true);
        SPEC = builder.build();
    }
    private ClientPreferences() { }
}
