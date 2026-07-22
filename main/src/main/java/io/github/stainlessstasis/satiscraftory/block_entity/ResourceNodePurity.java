package io.github.stainlessstasis.satiscraftory.block_entity;

import net.minecraft.util.RandomSource;

public enum ResourceNodePurity {
    IMPURE(25, 0.5f),
    NORMAL(50, 1f),
    PURE(25, 2f);

    private static int totalWeight = 0;

    private final int weight;
    private final float productionRateMultiplier;
    ResourceNodePurity(int weight, float productionRateMultiplier) {
        if (weight < 0) throw new IllegalStateException("Resource Node weight must be positive!");
        this.weight = weight;
        this.productionRateMultiplier = productionRateMultiplier;
    }

    public int getWeight() {
        return weight;
    }
    public float getProductionRateMultiplier() {
        return productionRateMultiplier;
    }

    public static float getWeightedChance(ResourceNodePurity purity) {
        return (float) purity.weight / getTotalWeight();
    }

    private static int getTotalWeight() {
        if (totalWeight < 0) {
            totalWeight = 0;
            for (var purity : values()) totalWeight += purity.weight;
        }
        return totalWeight;
    }

    public static ResourceNodePurity pickRandom(RandomSource random) {
        int roll = random.nextInt(getTotalWeight());
        int cumulative = 0;
        for (var purity : values()) {
            cumulative += purity.weight;
            if (roll < cumulative) return purity;
        }
        return NORMAL; // fallback, should be unreachable
    }
}
