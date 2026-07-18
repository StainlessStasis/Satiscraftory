package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.datagen.ManifoldRecipeProvider;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldMachineTypes;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.List;

public class MachineRecipesProvider extends ManifoldRecipeProvider {
    public MachineRecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addRecipes(RecipeOutput output) {
        output.accept(Satiscraftory.id("test"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.IRON_INGOT, 4)),
                List.of(RecipeIngredient.of(Items.IRON_CHAIN, 3)),
                40L
        ));
    }
}