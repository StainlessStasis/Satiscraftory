package io.github.stainlessstasis.manifold.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.stainlessstasis.manifold.client.block_preview.MultiblockGuiPreview;
import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class MultiblockPreviewPanel {
    private MultiblockPreviewPanel() {}

    public static void render(GuiGraphicsExtractor graphics, BlockItem blockItem, int centerX, int centerY, int size, long gameTime) {
        if (blockItem.getBlock() instanceof Multiblock<?> multiblock && MultiblockGuiPreview.rendererFor(multiblock) != null) {
            renderMultiblock(multiblock, centerX, centerY, size, gameTime);
        } else {
            renderItemIcon(graphics, blockItem, centerX, centerY, size);
        }
    }

    private static void renderItemIcon(GuiGraphicsExtractor graphics, BlockItem blockItem, int centerX, int centerY, int size) {
        ItemStack stack = new ItemStack(blockItem);
        float scale = size / 16f;
        GuiRenderUtils.scaledItem(graphics, stack, centerX, centerY, scale);
    }

    private static void renderMultiblock(Multiblock<?> multiblock, int centerX, int centerY, int size, long gameTime) {
        System.out.println("RENDER MULTIBLOCK");
        float yaw = (gameTime % 360L) * 1f;
        Direction facing = Direction.NORTH;

        PoseStack poseStack = new PoseStack();
        poseStack.translate(centerX, centerY, 100);
        poseStack.scale(size, -size, size);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        MultiblockGuiPreview.render(poseStack, bufferSource, multiblock, facing, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF);
        bufferSource.endBatch();
    }
}