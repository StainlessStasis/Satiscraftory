package io.github.stainlessstasis.manifold.factory_component;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * Representation of "a thing moving through the factory."
 */
public record Payload(Identifier itemId, int count, @Nullable Map<String, Object> extraData) {
    public Payload(Identifier itemId) {
        this(itemId, 1, null);
    }

    public Payload(Identifier itemId, int count) {
        this(itemId, count, null);
    }

    public boolean hasExtraData() {
        return extraData != null && !extraData.isEmpty();
    }

    public Payload withCount(int newCount) {
        return new Payload(itemId, newCount, extraData);
    }

    @Override
    public @NonNull String toString() {
        return "Payload(" + itemId + (count != 1 ? " x" + count : "") + ")";
    }
}

