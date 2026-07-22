package io.github.stainlessstasis.satiscraftory.block_entity;

public enum ResourceNodePurity {
    IMPURE(25, 0.5f),
    NORMAL(50, 1f),
    PURE(25, 2f);

    private static int totalWeight = 0;

    private final int weight;
    private final float resourceMultiplier;
    ResourceNodePurity(int weight, float resourceMultiplier) {
        if (weight < 0) throw new IllegalStateException("Resource Node weight must be positive!");
        this.weight = weight;
        this.resourceMultiplier = resourceMultiplier;
    }

    public int getWight() {
        return weight;
    }
    public float getResourceMultiplier() {
        return resourceMultiplier;
    }

    public static float getWeightedChance(ResourceNodePurity purity) {
        if (totalWeight <= 0) recalculateWeights();
        return (float) purity.getWight() / totalWeight;
    }

    private static void recalculateWeights() {
        for (var purity : ResourceNodePurity.values()) {
            totalWeight += purity.getWight();
        }
    }
}
