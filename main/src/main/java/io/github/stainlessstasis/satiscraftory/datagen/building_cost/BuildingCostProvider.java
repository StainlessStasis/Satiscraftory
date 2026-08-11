package io.github.stainlessstasis.satiscraftory.datagen.building_cost;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCost;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCosts;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BuildingCostProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    protected BuildingCostProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, BuildingCosts.PATH);
    }

    protected abstract void addCosts(CostOutput output);

    @FunctionalInterface
    public interface CostOutput {
        void accept(Identifier id, BuildingCost.Data data);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        addCosts((id, data) -> {
            Path path = pathProvider.file(id, "json");
            JsonElement json = BuildingCost.Data.CODEC.encodeStart(JsonOps.INSTANCE, data)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode building cost " + id + ": " + msg));
            futures.add(DataProvider.saveStable(cache, json, path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Building Costs";
    }
}