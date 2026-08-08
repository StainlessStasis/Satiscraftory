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

import java.util.*;
import java.util.stream.Stream;

public class ManifoldGeneratorFuels extends SimplePreparableReloadListener<Map<Identifier, GeneratorFuel>> {
    public static final String PATH = "generator_fuels";
    private static Map<Identifier, GeneratorFuel> FUELS = Map.of();
    private static Map<Identifier, Map<Identifier, GeneratorFuel>> ITEM_FUELS_BY_TYPE = Map.of();
    private static Map<Identifier, List<GeneratorFuel>> TAG_FUELS_BY_TYPE = Map.of();

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

        Map<Identifier, Map<Identifier, GeneratorFuel>> itemsByType = new HashMap<>();
        Map<Identifier, List<GeneratorFuel>> tagsByType = new HashMap<>();

        for (GeneratorFuel fuel : FUELS.values()) {
            if (fuel.itemId().isPresent()) {
                itemsByType.computeIfAbsent(fuel.generatorType(), _ -> new HashMap<>()).put(fuel.itemId().get(), fuel);
            } else {
                tagsByType.computeIfAbsent(fuel.generatorType(), _ -> new ArrayList<>()).add(fuel);
            }
        }

        Map<Identifier, Map<Identifier, GeneratorFuel>> immutableItemsByType = new HashMap<>();
        for (var entry : itemsByType.entrySet()) immutableItemsByType.put(entry.getKey(), Map.copyOf(entry.getValue()));
        ITEM_FUELS_BY_TYPE = Map.copyOf(immutableItemsByType);

        Map<Identifier, List<GeneratorFuel>> immutableTagsByType = new HashMap<>();
        for (var entry : tagsByType.entrySet()) immutableTagsByType.put(entry.getKey(), List.copyOf(entry.getValue()));
        TAG_FUELS_BY_TYPE = Map.copyOf(immutableTagsByType);

        long generatorTypeCount = Stream.concat(
                ITEM_FUELS_BY_TYPE.keySet().stream(),
                TAG_FUELS_BY_TYPE.keySet().stream()
        ).distinct().count();

        Manifold.LOGGER.info("ManifoldGeneratorFuels loaded {} fuels across {} generator type(s)", FUELS.size(), generatorTypeCount);
    }

    private static Identifier trimToFuelId(Identifier fileLocation) {
        String path = fileLocation.getPath();
        String trimmed = path.substring(PATH.length() + 1, path.length() - ".json".length());
        return Identifier.fromNamespaceAndPath(fileLocation.getNamespace(), trimmed);
    }

    public static @Nullable GeneratorFuel get(Identifier generatorType, Identifier itemId) {
        Map<Identifier, GeneratorFuel> matches = ITEM_FUELS_BY_TYPE.get(generatorType);
        if (matches != null) {
            GeneratorFuel fuel = matches.get(itemId);
            if (fuel != null) return fuel;
        }

        List<GeneratorFuel> tagFuels = TAG_FUELS_BY_TYPE.get(generatorType);
        if (tagFuels != null) {
            for (GeneratorFuel fuel : tagFuels) {
                if (fuel.matches(itemId)) return fuel;
            }
        }

        return null;
    }

    public static boolean isValidFuel(Identifier generatorType, Identifier itemId) {
        return get(generatorType, itemId) != null;
    }

    public static Map<Identifier, GeneratorFuel> allFuels() {
        return FUELS;
    }
}