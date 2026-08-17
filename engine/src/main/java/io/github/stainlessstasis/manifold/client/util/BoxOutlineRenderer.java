package io.github.stainlessstasis.manifold.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public final class BoxOutlineRenderer {
    private BoxOutlineRenderer() {}

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, BlockPos min, BlockPos max, Color color) {
        float sizeX = max.getX() - min.getX() + 1;
        float sizeY = max.getY() - min.getY() + 1;
        float sizeZ = max.getZ() - min.getZ() + 1;

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        float ox = (float) (min.getX() - camPos.x);
        float oy = (float) (min.getY() - camPos.y);
        float oz = (float) (min.getZ() - camPos.z);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) ->
                emitBoxOutline(pose, buffer, 0, 0, 0, sizeX, sizeY, sizeZ, r, g, b, a)
        );
        poseStack.popPose();
    }

    private static void emitBoxOutline(
            PoseStack.Pose pose, VertexConsumer buffer,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            int r, int g, int b, int a
    ) {
        // bottom
        edge(pose, buffer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        edge(pose, buffer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        edge(pose, buffer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        edge(pose, buffer, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // top
        edge(pose, buffer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        edge(pose, buffer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        edge(pose, buffer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        edge(pose, buffer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // verticals
        edge(pose, buffer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        edge(pose, buffer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        edge(pose, buffer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        edge(pose, buffer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void edge(
            PoseStack.Pose pose, VertexConsumer buffer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            int r, int g, int b, int a
    ) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > Constants.EPSILON) {
            dx /= length;
            dy /= length;
            dz /= length;
        }

        buffer.addVertex(pose, x0, y0, z0).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(2);
        buffer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, dx, dy, dz).setLineWidth(2);
    }
}