package io.github.stainlessstasis.manifold.client.factory_power;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.multiblock.PlacementPreview;
import io.github.stainlessstasis.manifold.factory_power.PowerLinkable;
import io.github.stainlessstasis.manifold.item.PowerLinkItem;
import io.github.stainlessstasis.manifold.network.PowerGridSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.awt.Color;

@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class CableRenderer {
    public static final Color CABLE_COLOR = new Color(0x36, 0x45, 0x4F);

    @SubscribeEvent
    public static void renderCables(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null) return;

        var edges = ClientPowerGrid.getEdges();
        BlockPos chainStart = ClientChainState.getChainStart();
        boolean holdingLinkItem = player.getMainHandItem().getItem() instanceof PowerLinkItem;
        boolean hasPreview = holdingLinkItem && chainStart != null;

        if (edges.isEmpty() && !hasPreview) return;

        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(RenderTypes.leash());

        for (PowerGridSyncPacket.Entry edge : edges) {
            Vec3 anchorA = CableGeometry.resolveAnchor(level, edge.posA());
            Vec3 anchorB = CableGeometry.resolveAnchor(level, edge.posB());
            CableGeometry.render(builder, event.getPoseStack(), level, cameraPos, anchorA, anchorB, CABLE_COLOR);
        }

        if (hasPreview) {
            renderPreview(builder, event, minecraft, level, player, cameraPos, chainStart);
        }

        bufferSource.endBatch(RenderTypes.leash());
    }

    private static void renderPreview(
            VertexConsumer builder, SubmitCustomGeometryEvent event, Minecraft minecraft,
            Level level, LocalPlayer player, Vec3 cameraPos, BlockPos chainStart
    ) {
        if (!(minecraft.hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof PowerLinkItem powerLinkItem)) {
            return;
        }
        BlockPos resolvedTargetPos = powerLinkItem.resolveLinkTarget(level, blockHitResult.getBlockPos());
        if (resolvedTargetPos == null || resolvedTargetPos.equals(chainStart)) return;

        boolean isValidTarget = level.getBlockEntity(resolvedTargetPos) instanceof PowerLinkable;
        Color previewColor = isValidTarget ? PlacementPreview.VALID_COLOR : PlacementPreview.INVALID_COLOR;

        Vec3 anchorA = CableGeometry.resolveAnchor(level, chainStart);
        Vec3 anchorB = CableGeometry.resolveAnchor(level, resolvedTargetPos);
        CableGeometry.render(builder, event.getPoseStack(), level, cameraPos, anchorA, anchorB, previewColor);
    }
}