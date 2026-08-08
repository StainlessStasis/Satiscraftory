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
import java.util.Map;

public class ManifoldGeneratorFuels extends SimplePreparableReloadListener<Map<Identifier, GeneratorFuel>> {
    public static final String PATH = "generator_fuels";
    private static Map<Identifier, GeneratorFuel> FUELS = Map.of();
    private static Map<Identifier, Map<Identifier, GeneratorFuel>> FUELS_BY_TYPE = Map.of();

    @Override
    protected @NonNull Map<Identifier, GeneratorFuel> prepare(ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        Map<Identifier, GeneratorFuel> loaded = new HashMap<>();

        resourceManager.listResources(PATH, path -> path.getPath().endsWith(".json"))
                .forEach((fileLocation, resource) -> {
                    Identifier fuelId = trimToFuelId(fileLocation);
                    try (var reader = resource.openAsReader()) {
                        JsonElement json = GsonHelper.parse(reader);
                        GeneratorFuel fuel = GeneratorFuel.Data.CODEC.parse(JsonOps.INSTANCE, json)
                                .getOrThrow(msg -> new IllegalStateException("Failed to parse " + fuelId + ": " + msg))
                                .withId(fuelId);
                        loaded.put(fuelId, fuel);
                    } catch (Exception e) {
                        Manifold.LOGGER.error("Skipping invalid generator fuel {}: {}", fileLocation, e.getMessage());
                    }
                });

        return loaded;
    }

    @Override
    protected void apply(@NonNull Map<Identifier, GeneratorFuel> loaded, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profiler) {
        FUELS = Map.copyOf(loaded);

        Map<Identifier, Map<Identifier, GeneratorFuel>> byType = new HashMap<>();
        for (GeneratorFuel fuel : FUELS.values()) {
            byType.computeIfAbsent(fuel.generatorType(), _ -> new HashMap<>()).put(fuel.itemId(), fuel);
        }
        Map<Identifier, Map<Identifier, GeneratorFuel>> immutableByType = new HashMap<>();
        for (var entry : byType.entrySet()) {
            immutableByType.put(entry.getKey(), Map.copyOf(entry.getValue()));
        }
        FUELS_BY_TYPE = Map.copyOf(immutableByType);

        Manifold.LOGGER.info("ManifoldGeneratorFuels loaded {} fuels across {} generator type(s)", FUELS.size(), FUELS_BY_TYPE.size());
    }

    private static Identifier trimToFuelId(Identifier fileLocation) {
        String path = fileLocation.getPath();
        String trimmed = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(fileLocation.getNamespace(), trimmed);
    }

    public static @Nullable GeneratorFuel get(Identifier generatorType, Identifier itemId) {
        Map<Identifier, GeneratorFuel> byItem = FUELS_BY_TYPE.get(generatorType);
        return byItem != null ? byItem.get(itemId) : null;
    }

    public static boolean isValidFuel(Identifier generatorType, Identifier itemId) {
        return get(generatorType, itemId) != null;
    }

    public static Map<Identifier, GeneratorFuel> fuelsForGeneratorType(Identifier generatorType) {
        return FUELS_BY_TYPE.getOrDefault(generatorType, Map.of());
    }

    public static Map<Identifier, GeneratorFuel> allFuels() {
        return FUELS;
    }
}
