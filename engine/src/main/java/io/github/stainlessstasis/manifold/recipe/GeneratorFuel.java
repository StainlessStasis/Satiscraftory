package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record GeneratorFuel(Identifier id, Identifier generatorType, Optional<Identifier> itemId, Optional<TagKey<Item>> tag, long burnTicks) {
    public GeneratorFuel {
        if (burnTicks <= 0) {
            throw new IllegalArgumentException("Generator fuel " + id + " must have a positive burn duration");
        }
        if (itemId.isPresent() == tag.isPresent()) {
            throw new IllegalArgumentException("Generator fuel " + id + " must specify exactly one of itemId or tag");
        }
    }

    public boolean matches(Identifier candidateItemId) {
        if (itemId.isPresent()) return itemId.get().equals(candidateItemId);

        Item item = BuiltInRegistries.ITEM.getOptional(candidateItemId).orElse(null);
        return item != null && tag.isPresent() && item.builtInRegistryHolder().is(tag.get());
    }

    public record Data(Optional<Identifier> item, Optional<Identifier> tag, Identifier generatorType, long burnTicks) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.optionalFieldOf("item").forGetter(Data::item),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(Data::tag),
                Identifier.CODEC.fieldOf("generatorType").forGetter(Data::generatorType),
                Codec.LONG.fieldOf("burnTicks").forGetter(Data::burnTicks)
        ).apply(i, Data::new));

        public Data {
            if (item.isPresent() == tag.isPresent()) {
                throw new IllegalArgumentException("Generator fuel data must specify exactly one of 'item' or 'tag'");
            }
        }

        public GeneratorFuel withId(Identifier id) {
            return new GeneratorFuel(id, generatorType, item, tag.map(t -> TagKey.create(Registries.ITEM, t)), burnTicks);
        }

        public static Data ofItem(Identifier generatorType, net.minecraft.world.level.ItemLike item, long burnTicks) {
            return new Data(Optional.of(BuiltInRegistries.ITEM.getKey(item.asItem())), Optional.empty(), generatorType, burnTicks);
        }

        public static Data ofTag(Identifier generatorType, TagKey<Item> tag, long burnTicks) {
            return new Data(Optional.empty(), Optional.of(tag.location()), generatorType, burnTicks);
        }
    }
}