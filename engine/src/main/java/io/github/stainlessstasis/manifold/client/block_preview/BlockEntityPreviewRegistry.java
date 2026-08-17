package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class BlockEntityPreviewRegistry {
    private BlockEntityPreviewRegistry(){}

    @FunctionalInterface
    public interface Renderer {
        void submitPreview(PoseStack poseStack, SubmitNodeCollector collector, Level level, BlockState previewState, BlockPos origin, int argbTint);
    }

    private static final Map<BlockEntityType<?>, Renderer> RENDERERS = new HashMap<>();

    public static void register(BlockEntityType<?> type, Renderer renderer) {
        RENDERERS.put(type, renderer);
    }

    public static @Nullable Renderer get(BlockEntityType<?> type) {
        return RENDERERS.get(type);
    }
}