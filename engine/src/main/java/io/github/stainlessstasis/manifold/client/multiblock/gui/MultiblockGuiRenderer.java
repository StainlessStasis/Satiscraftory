package io.github.stainlessstasis.manifold.client.multiblock.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockPreviewRegistry;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MultiblockGuiRenderer extends PictureInPictureRenderer<MultiblockGuiPreviewRenderState> {
    public MultiblockGuiRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public @NonNull Class<MultiblockGuiPreviewRenderState> getRenderStateClass() {
        return MultiblockGuiPreviewRenderState.class;
    }

    @Override
    protected @NonNull String getTextureLabel() {
        return "multiblock_gui";
    }

    @Override
    protected void renderToTexture(MultiblockGuiPreviewRenderState renderState, @NonNull PoseStack poseStack) {
        MultiblockRenderer<?, ?> renderer = rendererFor(renderState.multiblock());
        if (renderer == null) return;

        renderer.submitGuiPreviewToBuffer(
                poseStack, this.bufferSource, renderState.facing(),
                LightCoordsUtil.FULL_BRIGHT, renderState.tintColor(),
                renderState.gameTime() % 360L
        );
    }

    public static @Nullable MultiblockRenderer<?, ?> rendererFor(Multiblock<?> multiblock) {
        BaseEntityBlock block = multiblock.getPreviewBlock();
        BlockEntity blockEntity = block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState());
        if (blockEntity == null) return null;
        return MultiblockPreviewRegistry.get(blockEntity.getType());
    }
}