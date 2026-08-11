package io.github.stainlessstasis.satiscraftory.recipe;

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
import java.util.Map;

public class BuildingCosts extends SimplePreparableReloadListener<Map<Identifier, BuildingCost>> {
    public static final String PATH = "building_costs";
    private static Map<Identifier, BuildingCost> COSTS = Map.of();
    private static Map<Identifier, BuildingCost> COSTS_BY_BUILDING = Map.of();

    @Override
    protected @NonNull Map<Identifier, BuildingCost> prepare(ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        Map<Identifier, BuildingCost> loaded = new HashMap<>();

        resourceManager.listResources(PATH, path -> path.getPath().endsWith(".json"))
                .forEach((fileLocation, resource) -> {
                    Identifier costId = trimToCostId(fileLocation);
                    try (var reader = resource.openAsReader()) {
                        JsonElement json = GsonHelper.parse(reader);
                        BuildingCost cost = BuildingCost.Data.CODEC.parse(JsonOps.INSTANCE, json)
                                .getOrThrow(msg -> new IllegalStateException("Failed to parse " + costId + ": " + msg))
                                .withId(costId);
                        loaded.put(costId, cost);
                    } catch (Exception e) {
                        Manifold.LOGGER.error("Skipping invalid building cost {}: {}", fileLocation, e.getMessage());
                    }
                });

        return loaded;
    }

    @Override
    protected void apply(@NonNull Map<Identifier, BuildingCost> loaded, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        COSTS = Map.copyOf(loaded);

        Map<Identifier, BuildingCost> byBuilding = new HashMap<>();
        for (BuildingCost cost : COSTS.values()) {
            BuildingCost existing = byBuilding.put(cost.buildingItemId(), cost);
            if (existing != null) {
                Manifold.LOGGER.warn("Multiple building costs registered for {}, {} will be ignored", cost.buildingItemId(), existing.id());
            }
        }
        COSTS_BY_BUILDING = Map.copyOf(byBuilding);

        Manifold.LOGGER.info("ManifoldBuildingCosts loaded {} building cost(s)", COSTS.size());
    }

    private static Identifier trimToCostId(Identifier fileLocation) {
        String path = fileLocation.getPath();
        String trimmed = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(fileLocation.getNamespace(), trimmed);
    }

    public static @Nullable BuildingCost get(Identifier buildingId) {
        return COSTS_BY_BUILDING.get(buildingId);
    }

    public static Map<Identifier, BuildingCost> allCosts() {
        return COSTS;
    }
}