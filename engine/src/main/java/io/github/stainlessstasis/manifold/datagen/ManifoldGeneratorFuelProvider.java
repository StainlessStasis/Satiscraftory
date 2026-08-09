package io.github.stainlessstasis.manifold.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.stainlessstasis.manifold.recipe.GeneratorFuel;
import io.github.stainlessstasis.manifold.recipe.ManifoldGeneratorFuels;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ManifoldGeneratorFuelProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    protected ManifoldGeneratorFuelProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, ManifoldGeneratorFuels.PATH);
    }

    protected abstract void addFuels(FuelOutput output);

    public interface FuelOutput {
        void accept(Identifier id, GeneratorFuel.Data data);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addFuels((id, data) -> {
            Path path = pathProvider.file(id, "json");
            JsonElement json = GeneratorFuel.Data.CODEC.encodeStart(JsonOps.INSTANCE, data)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode generator fuel " + id + ": " + msg));
            futures.add(DataProvider.saveStable(cache, json, path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Manifold Generator Fuels";
    }
}
