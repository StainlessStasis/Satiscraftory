package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldMachineTypes;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;

public class MachineRecipeProvider extends ManifoldRecipeProvider {
    public MachineRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addRecipes(RecipeOutput output) {
        output.accept(Manifold.id("basic_processing"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.RAW_IRON, 1)),
                List.of(RecipeIngredient.of(Items.IRON_INGOT, 1)),
                40L
        ));
    }
}