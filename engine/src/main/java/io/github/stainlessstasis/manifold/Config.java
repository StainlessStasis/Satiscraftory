package io.github.stainlessstasis.manifold;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue BELTS_DROP_ITEMS = BUILDER
            .comment("Whether belts should drop their items when broken")
            .define("beltsDropItems", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
