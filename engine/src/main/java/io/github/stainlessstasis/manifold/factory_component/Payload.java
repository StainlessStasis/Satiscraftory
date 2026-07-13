package io.github.stainlessstasis.manifold.factory_component;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * Representation of "a thing moving through the factory."
 *
 * @param typeId Identifier of the real Item (e.g. "minecraft:stone").
 */
public record Payload(String typeId, int count, @Nullable Map<String, Object> extraData) {
    public Payload(String typeId) {
        this(typeId, 1, null);
    }

    public Payload(String typeId, int count) {
        this(typeId, count, null);
    }

    public boolean hasExtraData() {
        return extraData != null && !extraData.isEmpty();
    }

    public Payload withCount(int newCount) {
        return new Payload(typeId, newCount, extraData);
    }

    @Override
    public @NonNull String toString() {
        return "Payload(" + typeId + (count != 1 ? " x" + count : "") + ")";
    }
}

