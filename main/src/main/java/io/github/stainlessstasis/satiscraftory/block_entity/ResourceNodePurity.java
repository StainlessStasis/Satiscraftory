package io.github.stainlessstasis.satiscraftory.block_entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum ResourceNodePurity implements StringRepresentable {
    IMPURE("impure", 25, 0.5f),
    NORMAL("normal", 50, 1f),
    PURE("pure", 25, 2f);

    public static final Codec<ResourceNodePurity> CODEC = StringRepresentable.fromEnum(ResourceNodePurity::values);

    private static final int TOTAL_WEIGHT = computeTotalWeight();

    private final String serializedName;
    private final int weight;
    private final float productionRateMultiplier;

    ResourceNodePurity(String serializedName, int weight, float productionRateMultiplier) {
        if (weight < 0) throw new IllegalStateException("Resource Node weight must be positive!");
        this.serializedName = serializedName;
        this.weight = weight;
        this.productionRateMultiplier = productionRateMultiplier;
    }

    @Override
    public @NonNull String getSerializedName() {
        return serializedName;
    }

    public int getWeight() {
        return weight;
    }

    public float getProductionRateMultiplier() {
        return productionRateMultiplier;
    }

    public static float getWeightedChance(ResourceNodePurity purity) {
        return (float) purity.weight / TOTAL_WEIGHT;
    }

    public static ResourceNodePurity pickRandom(RandomSource random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (var purity : values()) {
            cumulative += purity.weight;
            if (roll < cumulative) return purity;
        }
        return NORMAL; // fallback, should be unreachable
    }

    private static int computeTotalWeight() {
        int total = 0;
        for (var purity : values()) total += purity.weight;
        return total;
    }
}