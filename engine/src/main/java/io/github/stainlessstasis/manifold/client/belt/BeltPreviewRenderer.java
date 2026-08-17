package io.github.stainlessstasis.manifold.client.belt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.client.block_preview.BlockEntityPreviewRegistry;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public final class BeltPreviewRenderer implements BlockEntityPreviewRegistry.Renderer {
    private final Vector3f scratchP0 = new Vector3f();
    private final Vector3f scratchP1 = new Vector3f();
    private final Vector3f scratchP2 = new Vector3f();
    private final Vector3f scratchP3 = new Vector3f();

    private static final int PREVIEW_SEGMENTS = 4;

    @Override
    public void submitPreview(PoseStack poseStack, SubmitNodeCollector collector, Level level,
                              BlockState previewState, BlockPos origin, int argbTint) {
        BeltShape shape = previewState.getValue(BeltBlock.SHAPE);
        boolean reversed = previewState.getValue(BeltBlock.REVERSED);

        TextureAtlasSprite sprite = BeltRenderUtils.spriteFor(shape);
        RenderType renderType = RenderTypes.entityTranslucent(sprite.atlasLocation());
        List<BeltGeometry.BeltStripQuad> quads = BeltGeometry.stripQuadsFor(shape, PREVIEW_SEGMENTS);
        boolean flip = BeltRenderUtils.needsMirror(shape, reversed);
        int count = quads.size();
        int light = LightCoordsUtil.FULL_BRIGHT;

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        poseStack.pushPose();
        poseStack.translate(origin.getX() - camPos.x, origin.getY() - camPos.y, origin.getZ() - camPos.z);

        collector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            VertexConsumer wrapped = sprite.wrap(vertexConsumer);
            for (int i = 0; i < count; i++) {
                float tStart = (float) i / count;
                float tEnd = (float) (i + 1) / count;
                BeltRenderUtils.emitArcSegment(
                        pose, wrapped, quads.get(i), tStart, tEnd, 0,
                        light, flip, argbTint, scratchP0, scratchP1, scratchP2, scratchP3
                );
            }
        });

        poseStack.popPose();
    }
}