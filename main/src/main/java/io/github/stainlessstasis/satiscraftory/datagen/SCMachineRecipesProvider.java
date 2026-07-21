package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.manifold.datagen.ManifoldRecipeProvider;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldMachineTypes;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.List;

public class SCMachineRecipesProvider extends ManifoldRecipeProvider {
    public SCMachineRecipesProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addRecipes(RecipeOutput output) {
        output.accept(Satiscraftory.id("iron_plate"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.IRON_INGOT, 3)),
                List.of(RecipeIngredient.of(SCItems.IRON_PLATE, 2)),
                120L
        ));
    }
}