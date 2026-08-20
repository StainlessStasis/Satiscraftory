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
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
    
    public static void submitBox(PoseStack poseStack, SubmitNodeCollector collector, BlockPos origin, Color tint) {
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(origin.getX() - camPos.x, origin.getY() - camPos.y, origin.getZ() - camPos.z);
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS, false),
                (pose, buffer) -> emitBoxQuads(pose, buffer, FULL_CUBE, tint)
        );
        poseStack.popPose();
    }

    private static final AABB FULL_CUBE = new AABB(0, 0, 0, 1, 1, 1);
    private static final Identifier BOX_TEX = Identifier.withDefaultNamespace("block/white_concrete");

    private static void emitBoxQuads(PoseStack.Pose pose, VertexConsumer buffer, AABB box, Color color) {
        SpriteId spriteId = new SpriteId(TextureAtlas.LOCATION_BLOCKS, BOX_TEX);
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(spriteId);

        int a = color.getAlpha(), r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        float u = sprite.getU0(), v = sprite.getV0();
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;

        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, 0, 0, -1);  // north
        emitFace(pose, buffer, u, v, r, g, b, a, x1, y0, z1, x0, y0, z1, x0, y1, z1, x1, y1, z1, 0, 0, 1);   // south
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z1, x0, y0, z0, x0, y1, z0, x0, y1, z1, -1, 0, 0);  // west
        emitFace(pose, buffer, u, v, r, g, b, a, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, 1, 0, 0);   // east
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, 0, 1, 0);   // top
        emitFace(pose, buffer, u, v, r, g, b, a, x0, y0, z1, x1, y0, z1, x1, y0, z0, x0, y0, z0, 0, -1, 0);  // bottom
    }

    private static void emitFace(
            PoseStack.Pose pose, VertexConsumer buffer,
            float u, float v,
            int r, int g, int b, int a,
            float x0, float y0, float z0, float x1, float y1, float z1,
            float x2, float y2, float z2, float x3, float y3, float z3,
            float nx, float ny, float nz
    ) {
        buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
        buffer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, nx, ny, nz);
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