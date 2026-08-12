package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SCCraftingRecipesProvider extends RecipeProvider {
    protected SCCraftingRecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);

        // ========== IRON ==========
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SCItems.IRON_PLATE, 2)
                .pattern("###")
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SCItems.IRON_ROD, 2)
                .pattern("#")
                .pattern("#")
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, SCItems.SCREWS, 4)
                .requires(SCItems.IRON_ROD, 1)
                .unlockedBy("has_iron_rod", has(SCItems.IRON_ROD))
                .save(output);

        // ========== COPPER ==========
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SCItems.COPPER_SHEET, 1)
                .pattern("##")
                .define('#', Items.COPPER_INGOT)
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SCItems.WIRE, 4)
                .pattern("#")
                .pattern("#")
                .define('#', Items.COPPER_INGOT)
                .unlockedBy("has_copper", has(Items.COPPER_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, SCItems.CABLE, 1)
                .pattern("#")
                .pattern("#")
                .define('#', SCItems.WIRE)
                .unlockedBy("has_wire", has(SCItems.WIRE))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull RecipeOutput output) {
            return new SCCraftingRecipesProvider(registries, output);
        }

        @Override
        public @NonNull String getName() {
            return "Satiscraftory Crafting Recipes";
        }
    }
}
