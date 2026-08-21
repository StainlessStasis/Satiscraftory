package io.github.stainlessstasis.satiscraftory.building.lane;

import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;

import java.util.ArrayList;
import java.util.List;

public final class LaneCosts {
    public static final int LANE_COST_UNIT_LENGTH = 16;

    private LaneCosts() {}

    public static List<RecipeIngredient> computeLaneCost(BuildingCost baseCost, int laneLength) {
        List<RecipeIngredient> result = new ArrayList<>(baseCost.inputs().size());
        for (RecipeIngredient ingredient : baseCost.inputs()) {
            int amount = Math.ceilDivExact(ingredient.amount() * laneLength, LANE_COST_UNIT_LENGTH);
            result.add(new RecipeIngredient(ingredient.itemId(), amount));
        }
        return result;
    }

    /**
     * The cost of a single belt block when no refund share is available for it
     */
    public static List<RecipeIngredient> perBlockFallbackCost(BuildingCost baseCost) {
        return computeLaneCost(baseCost, 1);
    }

    /**
     * Distributes a lane's total ingredient cost evenly across {@code beltCount} belts.
     * Guarantees that the sum of all belt refunds equals the original lane cost,
     * preventing duplication exploits (due to rounding) when belts are demolished individually
     */
    public static List<List<RecipeIngredient>> apportionRefundShares(List<RecipeIngredient> laneCost, int beltCount) {
        List<List<RecipeIngredient>> shares = new ArrayList<>(beltCount);
        for (int i = 0; i < beltCount; i++) {
            shares.add(new ArrayList<>());
        }

        for (RecipeIngredient ingredient : laneCost) {
            int total = ingredient.amount();
            int base = total / beltCount;
            int remainder = total % beltCount;

            for (int i = 0; i < beltCount; i++) {
                int amount = base + (i < remainder ? 1 : 0);
                if (amount > 0) {
                    shares.get(i).add(new RecipeIngredient(ingredient.itemId(), amount));
                }
            }
        }
        return shares;
    }
}