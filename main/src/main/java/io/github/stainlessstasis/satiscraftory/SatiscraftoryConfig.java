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

    public static final ModConfigSpec.BooleanValue GENERATE_RESOURCE_NODES = BUILDER
            .comment("Whether Resource Nodes should generate in newly generated chunks. " +
                    "Does not affect already generated nodes/chunks. MUST RESTART WORLD TO TAKE EFFECT!")
            .define("generateResourceNodes", true);

    public static final ModConfigSpec.IntValue RESOURCE_NODE_SCANNER_RANGE = BUILDER
            .comment("Maximum range, in blocks, at which the Resource Node Scanner can detect nodes within. " +
                    "Note that the scanner only finds nodes which have already generated and been cached. " +
                    "Increasing this range will not suddenly make it find more nodes if the chunks aren't already generated")
            .defineInRange("resourceNodeScannerRange", 1000, 1, 100000);

    static final ModConfigSpec SPEC = BUILDER.build();
}
