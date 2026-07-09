package com.example.examplemod.engine_internal.client;

import com.example.examplemod.engine_internal.Belt;
import com.example.examplemod.engine_internal.PayloadItems;
import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private static final double BELT_SPEED = 1d / BeltBlockEntity.LENGTH_TICKS;
    private static final int MAX_PREDICTED_TICKS = BeltBlockEntity.LENGTH_TICKS * 2;

    private final ItemModelResolver itemModelResolver;

    public BeltRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public @NonNull BeltRenderState createRenderState() {
        return new BeltRenderState();
    }

    @Override
    public void extractRenderState(
            @NonNull BeltBlockEntity blockEntity, @NonNull BeltRenderState renderState, float partialTick,
            @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        renderState.blockPos = blockEntity.getBlockPos();
        renderState.blockState = blockEntity.getBlockState();
        renderState.blockEntityType = blockEntity.getType();
        BlockPos abovePos = blockEntity.getBlockPos().above();
        renderState.lightCoords = blockEntity.getLevel() != null ? LevelRenderer.getLightCoords(blockEntity.getLevel(), abovePos) : 15728880;
        renderState.breakProgress = crumblingOverlay;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        List<Belt.ItemSnapshot> syncedItems = blockEntity.getRenderItems();
        long syncTick = blockEntity.getLastSyncTick();

        // Only rebuild ItemStacks/resolved item models when a new authoritative snapshot has actually arrived
        if (syncedItems != renderState.syncedItems) {
            renderState.syncedItems = syncedItems;
            rebuildItemCache(blockEntity, renderState, syncedItems);
        }
        renderState.syncTick = syncTick;

        double elapsedTicks = 0;
        if (blockEntity.getLevel() != null) {
            elapsedTicks = (blockEntity.getLevel().getGameTime() - syncTick) + partialTick;
        }
        double[] predictedPositions = predictPositions(syncedItems, elapsedTicks);

        for (int i = 0; i < renderState.items.size() && i < predictedPositions.length; i++) {
            renderState.items.get(i).position = predictedPositions[i];
        }
    }

    private void rebuildItemCache(BeltBlockEntity blockEntity, BeltRenderState renderState, List<Belt.ItemSnapshot> syncedItems) {
        renderState.items.clear();
        for (Belt.ItemSnapshot snapshot : syncedItems) {
            ItemStack itemStack = PayloadItems.toItemStack(snapshot.typeId(), 1);
            if (itemStack == null) continue;

            BeltRenderState.BeltItemRenderData itemRenderData = new BeltRenderState.BeltItemRenderData();
            itemRenderData.position = snapshot.position();
            itemRenderData.typeId = snapshot.typeId();
            itemModelResolver.updateForTopItem(itemRenderData.itemStackRenderState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
            renderState.items.add(itemRenderData);
        }
    }

    private double[] predictPositions(List<Belt.ItemSnapshot> syncedItems, double elapsedTicks) {
        double[] positions = new double[syncedItems.size()];
        for (int i = 0; i < syncedItems.size(); i++) positions[i] = syncedItems.get(i).position();

        if (elapsedTicks <= 0 || positions.length == 0) return positions;

        int fullTicks = (int) Math.min(Math.floor(elapsedTicks), MAX_PREDICTED_TICKS);
        double fraction = elapsedTicks - Math.floor(elapsedTicks);
        if (elapsedTicks > MAX_PREDICTED_TICKS) fraction = 0;

        for (int step = 0; step < fullTicks; step++) {
            advanceOnce(positions, 1.0);
        }
        if (fraction > 0) {
            advanceOnce(positions, fraction);
        }
        return positions;
    }

    private void advanceOnce(double[] positions, double stepScale) {
        for (int i = 0; i < positions.length; i++) {
            double cap = (i == 0) ? 1d : positions[i - 1] - BeltBlockEntity.MIN_GAP;
            double proposed = positions[i] + BELT_SPEED * stepScale;
            double newPos = Math.min(proposed, cap);
            if (newPos < 0) newPos = 0;
            positions[i] = newPos;
        }
    }

    @Override
    public void submit(
            BeltRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        for (BeltRenderState.BeltItemRenderData itemRenderData : renderState.items) {
            double travelOffset = itemRenderData.position - 0.5;
            double offsetX = renderState.facing.getStepX() * travelOffset;
            double offsetZ = renderState.facing.getStepZ() * travelOffset;

            poseStack.pushPose();
            poseStack.translate(0.5 + offsetX, 1.25, 0.5 + offsetZ);
            poseStack.scale(0.3f, 0.3f, 0.3f);

            itemRenderData.itemStackRenderState.submit(poseStack, collector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }
}