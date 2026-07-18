package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public record MachineRecipe(
        Identifier id,
        Identifier machineType,
        List<RecipeIngredient> inputs,
        List<RecipeIngredient> outputs,
        long durationTicks
) {
    public int inputCount() { return inputs.size(); }
    public int outputCount() { return outputs.size(); }

    public record Data(Identifier machineType, List<RecipeIngredient> inputs,
                       List<RecipeIngredient> outputs, long durationTicks) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                Identifier.CODEC.fieldOf("machineType").forGetter(Data::machineType),
                RecipeIngredient.CODEC.listOf().fieldOf("inputs").forGetter(Data::inputs),
                RecipeIngredient.CODEC.listOf().fieldOf("outputs").forGetter(Data::outputs),
                Codec.LONG.fieldOf("durationTicks").forGetter(Data::durationTicks)
        ).apply(i, Data::new));

        public MachineRecipe withId(Identifier id) {
            return new MachineRecipe(id, machineType, inputs, outputs, durationTicks);
        }

        public static Builder builder(Identifier machineType) { return new Builder(machineType); }

        public static final class Builder {
            private final Identifier machineType;
            private final List<RecipeIngredient> inputs = new ArrayList<>();
            private final List<RecipeIngredient> outputs = new ArrayList<>();
            private long duration = 100L;

            private Builder(Identifier machineType) { this.machineType = machineType; }

            public Builder input(ItemLike item, int amount) { inputs.add(RecipeIngredient.of(item, amount)); return this; }
            public Builder output(ItemLike item, int amount) { outputs.add(RecipeIngredient.of(item, amount)); return this; }
            public Builder duration(long ticks) { this.duration = ticks; return this; }

            public Data build() { return new Data(machineType, List.copyOf(inputs), List.copyOf(outputs), duration); }
        }
    }
}