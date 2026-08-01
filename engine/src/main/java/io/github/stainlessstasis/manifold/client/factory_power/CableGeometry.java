package io.github.stainlessstasis.manifold.client.factory_power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;

public final class CableGeometry {
    private static final int SEGMENTS = 24;
    private static final float CABLE_WIDTH = 0.05f;
    private static final float SAG = 0.35f;

    private CableGeometry() {}

    public static Vec3 resolveAnchor(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CableAnchorProvider anchorProvider) {
            return anchorProvider.getCableAnchorPos();
        }
        return Vec3.atCenterOf(pos);
    }

    public static void render(
            VertexConsumer builder, PoseStack poseStack, Level level, Vec3 cameraPos,
            Vec3 startPos, Vec3 endPos, Color color
    ) {
        float red = color.getRed() / 255f;
        float green = color.getGreen() / 255f;
        float blue = color.getBlue() / 255f;
        float alpha = color.getAlpha() / 255f;

        poseStack.pushPose();
        poseStack.translate(startPos.x - cameraPos.x, startPos.y - cameraPos.y, startPos.z - cameraPos.z);
        Matrix4f pose = poseStack.last().pose();

        float deltaX = (float) (endPos.x - startPos.x);
        float deltaY = (float) (endPos.y - startPos.y);
        float deltaZ = (float) (endPos.z - startPos.z);
        float offsetFactor = Mth.invSqrt(deltaX * deltaX + deltaZ * deltaZ) * CABLE_WIDTH / 2;
        float offsetX = deltaZ * offsetFactor;
        float offsetZ = deltaX * offsetFactor;

        Vec3 midpoint = startPos.lerp(endPos, 0.5);
        int lightCoords = LevelRenderer.getLightCoords(level, BlockPos.containing(midpoint));

        for (int step = 0; step <= SEGMENTS; step++) {
            addVertexPair(builder, pose, deltaX, deltaY, deltaZ, CABLE_WIDTH, offsetX, offsetZ, step, false, lightCoords, red, green, blue, alpha);
        }
        for (int step = SEGMENTS; step >= 0; step--) {
            addVertexPair(builder, pose, deltaX, deltaY, deltaZ, 0, offsetX, offsetZ, step, true, lightCoords, red, green, blue, alpha);
        }

        poseStack.popPose();
    }

    private static void addVertexPair(
            VertexConsumer builder, Matrix4f pose, float deltaX, float deltaY, float deltaZ, float fudge,
            float offsetX, float offsetZ, int step, boolean backwards, int lightCoords,
            float red, float green, float blue, float alpha
    ) {
        float progress = step / (float) SEGMENTS;
        float colorModifier = step % 2 == (backwards ? 1 : 0) ? 0.7f : 1;

        float x = deltaX * progress;
        float sag = SAG * progress * (1 - progress);
        float y = deltaY * progress - sag;
        float z = deltaZ * progress;

        builder.addVertex(pose, x - offsetX, y + fudge, z + offsetZ)
                .setColor(red * colorModifier, green * colorModifier, blue * colorModifier, alpha).setLight(lightCoords);
        builder.addVertex(pose, x + offsetX, y + CABLE_WIDTH - fudge, z - offsetZ)
                .setColor(red * colorModifier, green * colorModifier, blue * colorModifier, alpha).setLight(lightCoords);
    }
}