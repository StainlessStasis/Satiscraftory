package io.github.stainlessstasis.manifold.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ManifoldRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    protected ManifoldRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, ManifoldRecipes.PATH);
    }

    protected abstract void addRecipes(RecipeOutput output);

    public interface RecipeOutput {
        void accept(Identifier id, MachineRecipe.Data data);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addRecipes((id, data) -> {
            Path path = pathProvider.file(id, "json");
            JsonElement json = MachineRecipe.Data.CODEC.encodeStart(JsonOps.INSTANCE, data)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode recipe " + id + ": " + msg));
            futures.add(DataProvider.saveStable(cache, json, path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Manifold Machine Recipes";
    }
}