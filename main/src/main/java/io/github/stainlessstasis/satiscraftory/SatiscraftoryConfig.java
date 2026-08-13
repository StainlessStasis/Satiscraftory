package io.github.stainlessstasis.satiscraftory;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SatiscraftoryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue BUILDING_COSTS = BUILDER
            .comment("Whether buildings should require their respective items to be placed using the Build Gun")
            .define("buildingCosts", true);

    public static final ModConfigSpec.IntValue DEMOLITION_REFUND_PERCENT = BUILDER
            .comment("Percentage of a building's recipe cost refunded when it's demolished by the Build Gun")
            .defineInRange("demolitionRefundPercent", 100, 0, 100);
    public static int scaleForDemolishRefund(int amount) {
        return Math.round(amount * (DEMOLITION_REFUND_PERCENT.get() / 100f));
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}
