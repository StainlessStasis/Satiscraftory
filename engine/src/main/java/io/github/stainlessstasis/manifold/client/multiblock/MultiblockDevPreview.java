package io.github.stainlessstasis.manifold.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Constants;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.block_preview.MultiblockPreviewSubmission;
import io.github.stainlessstasis.manifold.client.block_preview.PlacementPreview;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public final class MultiblockDevPreview {
    private static @Nullable BlockPos anchorPos = null;
    private static @Nullable Multiblock<?> activePreviewer = null;

    private MultiblockDevPreview() {}

    public static void activate(BlockPos anchor, Multiblock<?> previewer) {
        anchorPos = anchor;
        activePreviewer = previewer;
        MultiblockShape shape = previewer.getMultiblockShape();
        Manifold.LOGGER.info("[MultiblockDevPreview] Anchor set at {} for shape {}x{}x{}",
                anchor, shape.width(), shape.height(), shape.depth());
    }

    public static void clear() {
        anchorPos = null;
        activePreviewer = null;
    }

    public static @Nullable BlockPos anchorPos() { return anchorPos; }

    public static @Nullable MultiblockShape activeShape() {
        return activePreviewer == null ? null : activePreviewer.getMultiblockShape();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isActive() {
        return anchorPos != null && activePreviewer != null;
    }

    @SuppressWarnings("DataFlowIssue") // isActive() already ensures anchor is not null
    @SubscribeEvent
    public static void renderPreview(SubmitCustomGeometryEvent event) {
        if (!isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        BlockPos anchor = anchorPos;
        Multiblock<?> previewer = activePreviewer;
        MultiblockShape shape = previewer.getMultiblockShape();

        BlockState previewState = previewer.getPreviewBlock().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        MultiblockPreviewSubmission.submit(
                event.getPoseStack(), event.getSubmitNodeCollector(), player.level(),
                previewer, previewState, anchor, Direction.NORTH, PlacementPreview.VALID_COLOR
        );

        renderBoundsWireframe(event.getPoseStack(), event.getSubmitNodeCollector(), shape, anchor);
    }

    private static void renderBoundsWireframe(PoseStack poseStack, SubmitNodeCollector collector, MultiblockShape shape, BlockPos anchor) {
        BlockPos min = shape.canonicalMin();
        BlockPos max = shape.canonicalMax();
        float minX = min.getX();
        float minY = min.getY();
        float minZ = min.getZ();
        float maxX = max.getX() + 1;
        float maxY = max.getY() + 1;
        float maxZ = max.getZ() + 1;

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        float ox = (float) (anchor.getX() - camPos.x);
        float oy = (float) (anchor.getY() - camPos.y);
        float oz = (float) (anchor.getZ() - camPos.z);

        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) ->
                emitBoxOutline(pose, buffer, minX, minY, minZ, maxX, maxY, maxZ));
        poseStack.popPose();
    }

    private static void emitBoxOutline(
            PoseStack.Pose pose, VertexConsumer buffer,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ
    ) {
        // bottom
        edge(pose, buffer, minX, minY, minZ, maxX, minY, minZ);
        edge(pose, buffer, maxX, minY, minZ, maxX, minY, maxZ);
        edge(pose, buffer, maxX, minY, maxZ, minX, minY, maxZ);
        edge(pose, buffer, minX, minY, maxZ, minX, minY, minZ);
        // top
        edge(pose, buffer, minX, maxY, minZ, maxX, maxY, minZ);
        edge(pose, buffer, maxX, maxY, minZ, maxX, maxY, maxZ);
        edge(pose, buffer, maxX, maxY, maxZ, minX, maxY, maxZ);
        edge(pose, buffer, minX, maxY, maxZ, minX, maxY, minZ);
        // verticals
        edge(pose, buffer, minX, minY, minZ, minX, maxY, minZ);
        edge(pose, buffer, maxX, minY, minZ, maxX, maxY, minZ);
        edge(pose, buffer, maxX, minY, maxZ, maxX, maxY, maxZ);
        edge(pose, buffer, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void edge(
            PoseStack.Pose pose, VertexConsumer buffer,
            float x0, float y0, float z0,
            float x1, float y1, float z1
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

        buffer.addVertex(pose, x0, y0, z0).setColor(255, 255, 0, 255).setNormal(pose, dx, dy, dz).setLineWidth(2);
        buffer.addVertex(pose, x1, y1, z1).setColor(255, 255, 0, 255).setNormal(pose, dx, dy, dz).setLineWidth(2);
    }
}