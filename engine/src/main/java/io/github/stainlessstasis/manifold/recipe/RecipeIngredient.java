package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public record RecipeIngredient(Identifier itemId, int amount) {
    public static final Codec<RecipeIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("itemId").forGetter(RecipeIngredient::itemId),
            Codec.INT.fieldOf("amount").forGetter(RecipeIngredient::amount)
    ).apply(i, RecipeIngredient::new));

    public static final StreamCodec<ByteBuf, RecipeIngredient> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, RecipeIngredient::itemId,
            ByteBufCodecs.INT, RecipeIngredient::amount,
            RecipeIngredient::new
    );

    public RecipeIngredient {
        if (amount <= 0) throw new IllegalArgumentException("Ingredient amount must be positive");
    }

    public static RecipeIngredient of(ItemLike item, int amount) {
        return new RecipeIngredient(BuiltInRegistries.ITEM.getKey(item.asItem()), amount);
    }
}