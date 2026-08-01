package io.github.stainlessstasis.manifold.client.factory_power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.network.PowerGridSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;

// TODO: should this be in the SC module? i guess it can stay here for now
@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class CableRenderer {
    private static final int SEGMENTS = 24;
    private static final float CABLE_WIDTH = 0.05f;
    private static final float SAG = 0.35f;

    @SubscribeEvent
    public static void renderCables(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) return;

        var edges = ClientPowerGrid.getEdges();
        if (edges.isEmpty()) return;

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.leash());

        for (PowerGridSyncPacket.Entry edge : edges) {
            Vec3 anchorA = resolveAnchor(level, edge.posA());
            Vec3 anchorB = resolveAnchor(level, edge.posB());
            renderCable(builder, event.getPoseStack(), level, cameraPos, anchorA, anchorB);
        }

        bufferSource.endBatch(RenderTypes.leash());
    }

    private static Vec3 resolveAnchor(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CableAnchorProvider anchorProvider) {
            return anchorProvider.getCableAnchorPos();
        }
        return Vec3.atCenterOf(pos);
    }

    private static void renderCable(
            VertexConsumer builder, PoseStack poseStack, Level level, Vec3 cameraPos, Vec3 startPos, Vec3 endPos
    ) {
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
            addVertexPair(builder, pose, deltaX, deltaY, deltaZ, CABLE_WIDTH, offsetX, offsetZ, step, false, lightCoords);
        }
        for (int step = SEGMENTS; step >= 0; step--) {
            addVertexPair(builder, pose, deltaX, deltaY, deltaZ, 0, offsetX, offsetZ, step, true, lightCoords);
        }

        poseStack.popPose();
    }

    private static void addVertexPair(
            VertexConsumer builder, Matrix4f pose, float deltaX, float deltaY, float deltaZ, float fudge,
            float offsetX, float offsetZ, int step, boolean backwards, int lightCoords
    ) {
        float progress = step / (float) SEGMENTS;
        float colorModifier = step % 2 == (backwards ? 1 : 0) ? 0.7f : 1;
        float red = 0.5f * colorModifier;
        float green = 0.4f * colorModifier;
        float blue = 0.3f * colorModifier;

        float x = deltaX * progress;
        float sag = SAG * progress * (1 - progress);
        float y = deltaY * progress - sag;
        float z = deltaZ * progress;

        builder.addVertex(pose, x - offsetX, y + fudge, z + offsetZ).setColor(red, green, blue, 1).setLight(lightCoords);
        builder.addVertex(pose, x + offsetX, y + CABLE_WIDTH - fudge, z - offsetZ).setColor(red, green, blue, 1).setLight(lightCoords);
    }
}