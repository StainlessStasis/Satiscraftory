package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record RecipeIngredient(Identifier itemId, int amount) {
    public static final Codec<RecipeIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("itemId").forGetter(RecipeIngredient::itemId),
            Codec.INT.fieldOf("amount").forGetter(RecipeIngredient::amount)
    ).apply(i, RecipeIngredient::new));

    public RecipeIngredient {
        if (amount <= 0) throw new IllegalArgumentException("Ingredient amount must be positive");
    }
}