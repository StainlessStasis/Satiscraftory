package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockPreviewRegistry;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public final class MultiblockPreviewSubmission {
    private MultiblockPreviewSubmission() {}

    public static void submit(
            PoseStack poseStack, SubmitNodeCollector collector, Level level,
            Multiblock<?> previewer, BlockState previewState,
            BlockPos origin, Direction facing, Color tint
    ) {
        BaseEntityBlock block = previewer.getPreviewBlock();
        BlockEntity blockEntity = block.newBlockEntity(BlockPos.ZERO, previewState);
        if (blockEntity == null) return;

        MultiblockRenderer<?, ?> renderer = MultiblockPreviewRegistry.get(blockEntity.getType());
        if (renderer == null) return;

        int light = LevelRenderer.getLightCoords(level, origin);
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(origin.getX() - camPos.x, origin.getY() - camPos.y, origin.getZ() - camPos.z);
        renderer.submitPreview(poseStack, collector, facing, light, tint.getRGB());
        poseStack.popPose();
    }
}