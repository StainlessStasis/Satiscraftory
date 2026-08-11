package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public final class BlockPreviewSubmission {
    private BlockPreviewSubmission() {}

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    public static void submit(
            PoseStack poseStack, SubmitNodeCollector collector,
            BlockState previewState, BlockPos origin, Color tint
    ) {
        BlockModelResolver resolver = Minecraft.getInstance().getBlockModelResolver();
        BlockModelRenderState renderState = new BlockModelRenderState();
        resolver.update(renderState, previewState, DISPLAY_CONTEXT);

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        float ox = (float) (origin.getX() - camPos.x);
        float oy = (float) (origin.getY() - camPos.y);
        float oz = (float) (origin.getZ() - camPos.z);

        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        renderState.submit(poseStack, collector, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint.getRGB());
        poseStack.popPose();
    }

}
