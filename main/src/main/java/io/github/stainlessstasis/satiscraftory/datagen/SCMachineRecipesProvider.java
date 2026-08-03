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
                6*20 // 20/min
        ));
        output.accept(Satiscraftory.id("iron_rod"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.IRON_INGOT, 1)),
                List.of(RecipeIngredient.of(SCItems.IRON_ROD, 1)),
                4*20 // 15/min
        ));
        output.accept(Satiscraftory.id("screws"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(SCItems.IRON_ROD, 1)),
                List.of(RecipeIngredient.of(SCItems.SCREWS, 4)),
                6*20 // 40/min
        ));

        output.accept(Satiscraftory.id("copper_ingot"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.RAW_COPPER, 1)),
                List.of(RecipeIngredient.of(Items.COPPER_INGOT, 1)),
                2*20 // 30/min
        ));
        output.accept(Satiscraftory.id("copper_sheet"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.COPPER_INGOT, 2)),
                List.of(RecipeIngredient.of(SCItems.COPPER_SHEET, 1)),
                6*20 // 10/min
        ));
        output.accept(Satiscraftory.id("wire"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(Items.COPPER_INGOT, 1)),
                List.of(RecipeIngredient.of(SCItems.WIRE, 2)),
                4*20 // 30/min
        ));
        output.accept(Satiscraftory.id("cable"), new MachineRecipe.Data(
                ManifoldMachineTypes.BASIC_MACHINE,
                List.of(RecipeIngredient.of(SCItems.WIRE, 2)),
                List.of(RecipeIngredient.of(SCItems.CABLE, 1)),
                2*20 // 30/min
        ));
    }
}