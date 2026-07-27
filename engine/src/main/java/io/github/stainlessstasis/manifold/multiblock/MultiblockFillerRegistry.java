package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class MultiblockFillerRegistry {
    private static final Map<Level, Map<BlockPos, BlockPos>> BY_LEVEL = new WeakHashMap<>();

    private MultiblockFillerRegistry() {}

    public static void register(Level level, BlockPos fillerPos, BlockPos controllerPos) {
        BY_LEVEL.computeIfAbsent(level, _ -> new ConcurrentHashMap<>())
                .put(fillerPos.immutable(), controllerPos.immutable());
    }

    public static void unregister(Level level, BlockPos fillerPos) {
        Map<BlockPos, BlockPos> forLevel = BY_LEVEL.get(level);
        if (forLevel != null) forLevel.remove(fillerPos);
    }

    public static @Nullable BlockPos controllerPosAt(Level level, BlockPos fillerPos) {
        Map<BlockPos, BlockPos> forLevel = BY_LEVEL.get(level);
        return forLevel != null ? forLevel.get(fillerPos) : null;
    }
}