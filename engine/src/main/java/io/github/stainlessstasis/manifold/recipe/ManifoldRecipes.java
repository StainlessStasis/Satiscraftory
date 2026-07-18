package io.github.stainlessstasis.manifold.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.stainlessstasis.manifold.Manifold;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManifoldRecipes extends SimplePreparableReloadListener<Map<Identifier, MachineRecipe>> {
    public static final String PATH = "machine_recipes";
    private static Map<Identifier, MachineRecipe> RECIPES = Map.of();

    @Override
    protected @NonNull Map<Identifier, MachineRecipe> prepare(ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        Map<Identifier, MachineRecipe> loaded = new HashMap<>();

        resourceManager.listResources(PATH, path -> path.getPath().endsWith(".json"))
                .forEach((fileLocation, resource) -> {
                    Identifier recipeId = trimToRecipeId(fileLocation);
                    try (var reader = resource.openAsReader()) {
                        JsonElement json = GsonHelper.parse(reader);
                        MachineRecipe recipe = MachineRecipe.Data.CODEC.parse(JsonOps.INSTANCE, json)
                                .getOrThrow(msg -> new IllegalStateException("Failed to parse " + recipeId + ": " + msg))
                                .withId(recipeId);
                        loaded.put(recipeId, recipe);
                    } catch (Exception e) {
                        Manifold.LOGGER.error("Skipping invalid machine recipe {}: {}", fileLocation, e.getMessage());
                    }
                });

        return loaded;
    }

    @Override
    protected void apply(@NonNull Map<Identifier, MachineRecipe> loaded, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        RECIPES = Map.copyOf(loaded);
        Manifold.LOGGER.info("ManifoldRecipes loaded {} recipes", RECIPES.size());
    }

    private static Identifier trimToRecipeId(Identifier fileLocation) {
        String path = fileLocation.getPath();
        String trimmed = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(fileLocation.getNamespace(), trimmed);
    }

    public static @Nullable MachineRecipe get(Identifier id) {
        return RECIPES.get(id);
    }

    public static Map<Identifier, MachineRecipe> allRecipes() {
        return RECIPES;
    }

    public static List<MachineRecipe> recipesForMachineType(Identifier machineType) {
        return RECIPES.values().stream().filter(r -> r.machineType().equals(machineType)).toList();
    }
}