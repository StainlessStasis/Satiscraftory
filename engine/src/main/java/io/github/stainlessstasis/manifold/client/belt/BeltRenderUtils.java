package io.github.stainlessstasis.manifold.client.belt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;

import static com.mojang.math.Constants.EPSILON;

public final class BeltRenderUtils {
    private static final Identifier STRAIGHT_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_straight");
    private static final Identifier CURVED_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_curved");
    private static final Identifier ASCENDING_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_ascending");

    private BeltRenderUtils() {}

    public static TextureAtlasSprite spriteFor(BeltShape shape) {
        Identifier tex = shape.isCorner() ? CURVED_TEX : (shape.isAscending() ? ASCENDING_TEX : STRAIGHT_TEX);
        SpriteId spriteId = new SpriteId(TextureAtlas.LOCATION_BLOCKS, tex);
        return Minecraft.getInstance().getAtlasManager().get(spriteId);
    }

    public static boolean needsMirror(BeltShape shape, boolean reversed) {
        if (reversed) {
            return shape.isCorner();
        } else {
            return shape == BeltShape.EAST_WEST || shape == BeltShape.NORTH_SOUTH;
        }
    }

    public static void emitQuadSegment(
            PoseStack.Pose pose, VertexConsumer buffer, BeltGeometry.BeltStripQuad quad,
            float geomStart, float geomEnd, float vStart, float vEnd, int light, int argb,
            Vector3f scratch0, Vector3f scratch1, Vector3f scratch2, Vector3f scratch3
    ) {
        Vector3f p0 = quad.pointAt(geomStart, 0, scratch0), p1 = quad.pointAt(geomStart, 1, scratch1);
        Vector3f p2 = quad.pointAt(geomEnd, 1, scratch2), p3 = quad.pointAt(geomEnd, 0, scratch3);

        int a = (argb >>> 24) & 0xFF, r = (argb >>> 16) & 0xFF, g = (argb >>> 8) & 0xFF, b = argb & 0xFF;

        buffer.addVertex(pose, p0.x, p0.y, p0.z)
                .setColor(r, g, b, a).setUv(quad.u0(), vStart)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p1.x, p1.y, p1.z)
                .setColor(r, g, b, a).setUv(quad.u1(), vStart)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p2.x, p2.y, p2.z)
                .setColor(r, g, b, a).setUv(quad.u1(), vEnd)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p3.x, p3.y, p3.z)
                .setColor(r, g, b, a).setUv(quad.u0(), vEnd)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    public static void emitArcSegment(
            PoseStack.Pose pose, VertexConsumer buffer, BeltGeometry.BeltStripQuad quad,
            float tStart, float tEnd, double phase, int light, boolean flip, int argb,
            Vector3f scratch0, Vector3f scratch1, Vector3f scratch2, Vector3f scratch3
    ) {
        float span = quad.v1() - quad.v0();
        float vStart = (float) (quad.v0() + span * wrap01(tStart + phase));
        float vEndRaw = vStart + span * (tEnd - tStart);

        if (vEndRaw <= quad.v1() + EPSILON) {
            emitQuadSegment(pose, buffer, quad, 0f, 1f,
                    reflect(vStart, quad, flip), reflect(vEndRaw, quad, flip), light, argb,
                    scratch0, scratch1, scratch2, scratch3);
        } else {
            float overflow = vEndRaw - quad.v1();
            float splitT = 1f - overflow / (span * (tEnd - tStart));
            emitQuadSegment(pose, buffer, quad, 0f, splitT,
                    reflect(vStart, quad, flip), reflect(quad.v1(), quad, flip), light, argb,
                    scratch0, scratch1, scratch2, scratch3);
            emitQuadSegment(pose, buffer, quad, splitT, 1f,
                    reflect(quad.v0(), quad, flip), reflect(quad.v0() + overflow, quad, flip), light, argb,
                    scratch0, scratch1, scratch2, scratch3);
        }
    }

    private static float reflect(float v, BeltGeometry.BeltStripQuad quad, boolean flip) {
        return flip ? (quad.v0() + quad.v1() - v) : v;
    }

    public static double wrap01(double v) {
        double w = v % 1;
        return w < 0 ? w + 1 : w;
    }
}