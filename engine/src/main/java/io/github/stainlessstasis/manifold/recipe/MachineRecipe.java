package io.github.stainlessstasis.manifold.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

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
    }
}