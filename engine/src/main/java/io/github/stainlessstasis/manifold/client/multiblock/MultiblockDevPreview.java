package io.github.stainlessstasis.manifold.client.multiblock;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.block_preview.MultiblockPreviewSubmission;
import io.github.stainlessstasis.manifold.client.block_preview.PlacementPreview;
import io.github.stainlessstasis.manifold.client.util.BoxOutlineRenderer;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

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

    private static final Color WIREFRAME_COLOR = new Color(0, 255, 255, 255);

    private static void renderBoundsWireframe(PoseStack poseStack, SubmitNodeCollector collector, MultiblockShape shape, BlockPos anchor) {
        BlockPos min = anchor.offset(shape.canonicalMin());
        BlockPos max = anchor.offset(shape.canonicalMax());
        BoxOutlineRenderer.render(poseStack, collector, min, max, WIREFRAME_COLOR);
    }
}