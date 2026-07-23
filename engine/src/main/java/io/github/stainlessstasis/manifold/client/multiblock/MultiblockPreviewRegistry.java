package io.github.stainlessstasis.manifold.client.multiblock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public final class MultiblockPreviewRegistry {
    private static final Map<BlockEntityType<?>, MultiblockRenderer<?, ?>> RENDERERS = new HashMap<>();

    private MultiblockPreviewRegistry() {}

    public static void register(BlockEntityType<?> type, MultiblockRenderer<?, ?> renderer) {
        RENDERERS.put(type, renderer);
    }

    public static @Nullable MultiblockRenderer<?, ?> get(BlockEntityType<?> type) {
        return RENDERERS.get(type);
    }
}