package io.github.stainlessstasis.manifold.client.block_preview;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class BlockPreviewSubmission {
    /// this isn't just some random bs, see MODEL_SEED in {@link BlockModelResolver} (it's private)
    private static final long MODEL_SEED = 42L;

    private BlockPreviewSubmission() {}

    /**
     * @return false if the model for {@code previewState} has no model parts; true otherwise
     */
    public static boolean submit(
            PoseStack poseStack, SubmitNodeCollector collector, ClientLevel level,
            BlockState previewState, BlockPos origin, Color tint
    ) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(previewState);

        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(level, origin, previewState, RandomSource.create(MODEL_SEED), parts);
        if (!hasAnyQuads(parts)) return false;

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        float ox = (float) (origin.getX() - camPos.x);
        float oy = (float) (origin.getY() - camPos.y);
        float oz = (float) (origin.getZ() - camPos.z);

        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS, false),
                (pose, buffer) -> emitParts(pose, buffer, parts, tint.getRGB())
        );
        poseStack.popPose();
        return true;
    }

    private static boolean hasAnyQuads(List<BlockStateModelPart> parts) {
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                if (!part.getQuads(direction).isEmpty()) return true;
            }
            if (!part.getQuads(null).isEmpty()) return true;
        }
        return false;
    }

    private static void emitParts(PoseStack.Pose pose, VertexConsumer buffer, List<BlockStateModelPart> parts, int argbTint) {
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                emitQuads(pose, buffer, part.getQuads(direction), argbTint);
            }
            emitQuads(pose, buffer, part.getQuads(null), argbTint);
        }
    }

    private static void emitQuads(PoseStack.Pose pose, VertexConsumer buffer, List<BakedQuad> quads, int argbTint) {
        for (BakedQuad quad : quads) {
            Direction facing = quad.direction();
            float nx = facing.getStepX();
            float ny = facing.getStepY();
            float nz = facing.getStepZ();

            for (int i = 0; i < 4; i++) {
                Vector3fc pos = quad.position(i);
                long packedUv = quad.packedUV(i);
                float u = UVPair.unpackU(packedUv);
                float v = UVPair.unpackV(packedUv);

                buffer.addVertex(pose, pos.x(), pos.y(), pos.z())
                        .setColor(argbTint)
                        .setUv(u, v)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightCoordsUtil.FULL_BRIGHT)
                        .setNormal(pose, nx, ny, nz);
            }
        }
    }
}