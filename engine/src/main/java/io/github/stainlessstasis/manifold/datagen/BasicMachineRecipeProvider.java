package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;

public class BasicMachineRecipeProvider extends ManifoldRecipeProvider {
    public BasicMachineRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addRecipes(RecipeOutput output) {
        output.accept(Manifold.id("basic_processing"), new MachineRecipe.Data(
                Manifold.id("basic_machine"),
                List.of(new RecipeIngredient(Identifier.parse(Items.RAW_IRON.toString()), 1)),
                List.of(new RecipeIngredient(Identifier.parse(Items.IRON_INGOT.toString()), 1)),
                20L
        ));
    }
}