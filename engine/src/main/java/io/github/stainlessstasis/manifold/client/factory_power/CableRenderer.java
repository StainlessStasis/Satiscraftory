package io.github.stainlessstasis.manifold.client.factory_power;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_power.PowerLinkable;
import io.github.stainlessstasis.manifold.item.PowerLinkItem;
import io.github.stainlessstasis.manifold.network.PowerGridSyncPacket;
import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import static io.github.stainlessstasis.manifold.client.multiblock.PlacementPreviewSubmission.VALID_TINT;

// TODO: should this be in the SC module? i guess it can stay here for now
@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class CableRenderer {
    private static final float RED = 0.5f, GREEN = 0.4f, BLUE = 0.3f;
    // TODO: move the valid tint to a shared constants class
    private static final float PREVIEW_ALPHA = ((VALID_TINT >> 24) & 0xFF) / 255f;
    private static final float PREVIEW_RED   = ((VALID_TINT >> 16) & 0xFF) / 255f;
    private static final float PREVIEW_GREEN = ((VALID_TINT >> 8)  & 0xFF) / 255f;
    private static final float PREVIEW_BLUE  = ( VALID_TINT        & 0xFF) / 255f;

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
            Vec3 anchorA = CableGeometry.resolveAnchor(level, edge.posA());
            Vec3 anchorB = CableGeometry.resolveAnchor(level, edge.posB());
            CableGeometry.render(builder, event.getPoseStack(), level, cameraPos, anchorA, anchorB, RED, GREEN, BLUE, 1);
        }

        BlockPos lookingAtPos = null;
        if (minecraft.hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() == HitResult.Type.BLOCK) {
            lookingAtPos = blockHitResult.getBlockPos();
        }

        boolean isLookingAtPowerLinkable = false;
        if (lookingAtPos != null && level.getBlockEntity(lookingAtPos) instanceof PowerLinkable) {
            isLookingAtPowerLinkable = true;
        }

        BlockPos chainStart = ClientChainState.getChainStart();
        if (player.getMainHandItem().getItem() instanceof PowerLinkItem && chainStart != null && isLookingAtPowerLinkable) {
            Vec3 anchorA = CableGeometry.resolveAnchor(level, chainStart);
            Vec3 anchorB = CableGeometry.resolveAnchor(level, lookingAtPos);
            CableGeometry.render(builder, event.getPoseStack(), level, cameraPos, anchorA, anchorB, PREVIEW_RED, PREVIEW_GREEN, PREVIEW_BLUE, PREVIEW_ALPHA);
        }

        bufferSource.endBatch(RenderTypes.leash());
    }
}
