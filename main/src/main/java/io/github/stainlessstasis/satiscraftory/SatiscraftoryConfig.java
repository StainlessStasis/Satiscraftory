package io.github.stainlessstasis.satiscraftory;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SatiscraftoryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue BUILDING_COSTS = BUILDER
            .comment("Whether buildings should require their respective items to be placed using the Build Gun (includes players in creative)")
            .define("buildingCosts", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
