package io.github.stainlessstasis.satiscraftory.building;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * The material cost required to place a building (used by build gun)
 */
public record BuildingCost(Identifier id, Identifier buildingItemId, List<RecipeIngredient> inputs) {
    public BuildingCost {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("BuildingCost " + id + " must have at least one item");
        }
    }

    public record Data(Identifier buildingItemId, List<RecipeIngredient> inputs) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("buildingItemId").forGetter(Data::buildingItemId),
                RecipeIngredient.CODEC.listOf().fieldOf("inputs").forGetter(Data::inputs)
        ).apply(i, Data::new));

        public BuildingCost withId(Identifier id) {
            return new BuildingCost(id, buildingItemId, inputs);
        }

        public static Builder builder(ItemLike building) {
            return new Builder(BuiltInRegistries.ITEM.getKey(building.asItem()));
        }

        public static class Builder {
            private final Identifier buildingItemId;
            private final List<RecipeIngredient> inputs = new ArrayList<>();

            private Builder(Identifier buildingId) {
                this.buildingItemId = buildingId;
            }

            public Builder input(ItemLike item, int amount) {
                inputs.add(RecipeIngredient.of(item, amount));
                return this;
            }
            public Data build() {
                return new Data(buildingItemId, List.copyOf(inputs));
            }
        }
    }
}