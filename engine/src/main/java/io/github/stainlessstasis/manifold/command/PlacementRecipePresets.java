package io.github.stainlessstasis.manifold.command;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlacementRecipePresets {
    private static final Map<UUID, Identifier> PRESETS = new ConcurrentHashMap<>();

    private PlacementRecipePresets() {}

    public static void set(UUID playerId, Identifier recipeId) {
        PRESETS.put(playerId, recipeId);
    }

    public static void clear(UUID playerId) {
        PRESETS.remove(playerId);
    }

    public static Identifier get(UUID playerId) {
        return PRESETS.get(playerId);
    }
}