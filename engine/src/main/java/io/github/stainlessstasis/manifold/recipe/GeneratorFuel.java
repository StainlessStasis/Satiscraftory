package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

public record GeneratorFuel(Identifier id, Identifier generatorType, Identifier itemId, long burnTicks) {
    public GeneratorFuel {
        if (burnTicks <= 0) {
            throw new IllegalArgumentException("Generator fuel " + id + " must have a positive burn duration");
        }
    }

    public record Data(Identifier generatorType, Identifier itemId, long burnTicks) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("generatorType").forGetter(Data::generatorType),
                Identifier.CODEC.fieldOf("itemId").forGetter(Data::itemId),
                Codec.LONG.fieldOf("burnTicks").forGetter(Data::burnTicks)
        ).apply(i, Data::new));

        public GeneratorFuel withId(Identifier id) {
            return new GeneratorFuel(id, generatorType, itemId, burnTicks);
        }

        public static Data of(Identifier generatorType, ItemLike item, long burnTicks) {
            return new Data(generatorType, BuiltInRegistries.ITEM.getKey(item.asItem()), burnTicks);
        }
    }
}
