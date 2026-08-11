package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockPreviewRegistry;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class MultiblockGuiPreview {
    private MultiblockGuiPreview() {}

    /**
     * @return false if this multiblock has no registered renderer, so that something like a flat item icon can be used as a fallback
     */
    public static boolean render(
            PoseStack poseStack, MultiBufferSource bufferSource, Multiblock<?> multiblock, Direction facing, int lightCoords, int tintColor
    ) {
        BaseEntityBlock block = multiblock.getPreviewBlock();
        BlockState previewState = block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        BlockEntity blockEntity = block.newBlockEntity(BlockPos.ZERO, previewState);
        if (blockEntity == null) return false;

        MultiblockRenderer<?, ?> renderer = MultiblockPreviewRegistry.get(blockEntity.getType());
        if (renderer == null) return false;

        renderer.renderInGui(poseStack, bufferSource, facing, lightCoords, tintColor);
        return true;
    }

    public static @Nullable MultiblockRenderer<?, ?> rendererFor(Multiblock<?> multiblock) {
        BaseEntityBlock block = multiblock.getPreviewBlock();
        BlockEntity blockEntity = block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (blockEntity == null) return null;
        return MultiblockPreviewRegistry.get(blockEntity.getType());
    }
}