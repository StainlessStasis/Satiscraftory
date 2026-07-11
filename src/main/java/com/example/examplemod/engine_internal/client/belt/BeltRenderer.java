package com.example.examplemod.engine_internal.client.belt;

import com.example.examplemod.engine_internal.Belt;
import com.example.examplemod.engine_internal.PayloadItems;
import com.example.examplemod.engine_internal.block.belt.BeltBlock;
import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Brightness;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.math.Constants.EPSILON;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private static final double BELT_SPEED = 1d / BeltBlockEntity.LENGTH_TICKS;
    private static final int MAX_PREDICTED_TICKS = BeltBlockEntity.LENGTH_TICKS * 2;
    private static final float Z_FIGHTING_ADJUSTMENT = 0.001f;

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
        renderState.lightCoords = blockEntity.getLevel() != null ? LevelRenderer.getLightCoords(blockEntity.getLevel(), abovePos) : Brightness.FULL_BRIGHT.block();
        renderState.breakProgress = crumblingOverlay;
        renderState.shape = blockEntity.getBlockState().getValue(BeltBlock.SHAPE);
        renderState.reversed = blockEntity.getBlockState().getValue(BeltBlock.REVERSED);

        List<Belt.ItemSnapshot> syncedItems = blockEntity.getRenderItems();
        long syncTick = blockEntity.getLastSyncedTick();

        if (syncedItems != renderState.syncedItems) {
            renderState.syncedItems = syncedItems;
            updateItemCache(blockEntity, renderState, syncedItems);
        }
        renderState.syncTick = syncTick;

        double elapsedTicks = elapsedTicksSince(blockEntity, syncTick, partialTick);
        double[] predictedPositions = predictPositions(syncedItems, elapsedTicks);
        for (int i = 0; i < renderState.items.size() && i < predictedPositions.length; i++) {
            renderState.items.get(i).position = predictedPositions[i];
        }

        BeltBlockEntity output = getNeighborBeltAt(blockEntity, blockEntity.resolveOutputPos());
        boolean outputHasRoom = output == null || hasRoomAtBack(output, partialTick);
        double rawFront = frontRawPosition(syncedItems, elapsedTicks);
        renderState.hideFrontItem = outputHasRoom && rawFront >= 1d - EPSILON;

        updateIncomingItem(blockEntity, renderState, partialTick);
    }

    private double frontRawPosition(List<Belt.ItemSnapshot> syncedItems, double elapsedTicks) {
        if (syncedItems.isEmpty()) return Double.NEGATIVE_INFINITY;
        return syncedItems.getFirst().position() + BELT_SPEED * Math.max(elapsedTicks, 0);
    }

    private double elapsedTicksSince(BeltBlockEntity belt, long syncTick, float partialTick) {
        if (belt.getLevel() == null) return 0;
        return (belt.getLevel().getGameTime() - syncTick) + partialTick;
    }

    private boolean hasRoomAtBack(BeltBlockEntity belt, float partialTick) {
        List<Belt.ItemSnapshot> synced = belt.getRenderItems();
        if (synced.isEmpty()) return true;
        double elapsed = elapsedTicksSince(belt, belt.getLastSyncedTick(), partialTick);
        double[] positions = predictPositions(synced, elapsed);
        return positions[positions.length - 1] >= BeltBlockEntity.MIN_GAP;
    }

    /**
     * If the upstream neighbor's front item has crossed the boundary into this belt,
     * draw it here and continue from exactly where the upstream belt says it is,
     * so it hands off smoothly
     */
    private void updateIncomingItem(BeltBlockEntity self, BeltRenderState renderState, float partialTick) {
        renderState.itemIncomingActive = false;

        BeltBlockEntity input = getNeighborBeltAt(self, self.resolveInputPos());
        if (input == null) return;
        // only trust it as a feeder if its own output actually resolves back to this belt
        if (!self.getBlockPos().equals(input.resolveOutputPos())) return;

        List<Belt.ItemSnapshot> inputSynced = input.getRenderItems();
        if (inputSynced.isEmpty()) return;

        double inputElapsed = elapsedTicksSince(input, input.getLastSyncedTick(), partialTick);
        double inputRawFront = frontRawPosition(inputSynced, inputElapsed);
        if (inputRawFront < 1d - EPSILON) return;

        if (!hasRoomAtBack(self, partialTick)) return;

        double overflow = Math.clamp(inputRawFront - 1d, 0d, 1d);
        String typeId = inputSynced.getFirst().typeId();
        renderState.itemIncomingActive = true;
        renderState.itemIncoming.position = overflow;
        renderState.itemIncoming.typeId = typeId;

        if (!typeId.equals(renderState.itemIncomingTypeCached)) {
            renderState.itemIncomingTypeCached = typeId;
            ItemStack stack = PayloadItems.toItemStack(typeId, 1);
            if (stack != null) {
                itemModelResolver.updateForTopItem(
                        renderState.itemIncoming.itemStackRenderState, stack, ItemDisplayContext.FIXED, self.getLevel(), null, 0);
            }
        }
    }

    private @Nullable BeltBlockEntity getNeighborBeltAt(BeltBlockEntity self, BlockPos pos) {
        if (self.getLevel() == null) return null;
        BlockEntity neighbor = self.getLevel().getBlockEntity(pos);
        return neighbor instanceof BeltBlockEntity neighborBelt ? neighborBelt : null;
    }

    private void updateItemCache(BeltBlockEntity blockEntity, BeltRenderState renderState, List<Belt.ItemSnapshot> syncedItems) {
        List<BeltRenderState.BeltItemRenderData> oldItems = new ArrayList<>(renderState.items);
        int oldSize = oldItems.size();
        int newSize = syncedItems.size();
        int oldStart = (oldSize > newSize) ? oldSize - newSize : 0;

        renderState.items.clear();
        for (int i = 0; i < newSize; i++) {
            Belt.ItemSnapshot snapshot = syncedItems.get(i);
            int reuseIndex = oldStart + i;
            BeltRenderState.BeltItemRenderData reused =
                    (reuseIndex < oldSize && oldItems.get(reuseIndex).typeId.equals(snapshot.typeId()))
                            ? oldItems.get(reuseIndex) : null;

            if (reused != null) {
                reused.position = snapshot.position();
                renderState.items.add(reused);
            } else {
                renderState.items.add(buildItemRenderData(blockEntity, snapshot));
            }
        }
    }

    private BeltRenderState.BeltItemRenderData buildItemRenderData(BeltBlockEntity blockEntity, Belt.ItemSnapshot snapshot) {
        BeltRenderState.BeltItemRenderData itemRenderData = new BeltRenderState.BeltItemRenderData();
        itemRenderData.position = snapshot.position();
        itemRenderData.typeId = snapshot.typeId();

        ItemStack itemStack = PayloadItems.toItemStack(snapshot.typeId(), 1);
        if (itemStack != null) {
            itemModelResolver.updateForTopItem(itemRenderData.itemStackRenderState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }
        return itemRenderData;
    }

    private double[] predictPositions(List<Belt.ItemSnapshot> syncedItems, double elapsedTicks) {
        double[] positions = new double[syncedItems.size()];
        for (int i = 0; i < syncedItems.size(); i++) positions[i] = syncedItems.get(i).position();

        if (elapsedTicks <= 0 || positions.length == 0) return positions;

        int fullTicks = (int) Math.min(Math.floor(elapsedTicks), MAX_PREDICTED_TICKS);
        double fraction = elapsedTicks - Math.floor(elapsedTicks);
        if (elapsedTicks > MAX_PREDICTED_TICKS) fraction = 0;

        for (int step = 0; step < fullTicks; step++) advanceOnce(positions, 1.0);
        if (fraction > 0) advanceOnce(positions, fraction);
        return positions;
    }

    private void advanceOnce(double[] positions, double stepScale) {
        for (int i = 0; i < positions.length; i++) {
            double cap = (i == 0) ? 1d : Math.max(positions[i - 1] - BeltBlockEntity.MIN_GAP, 0);
            double proposed = positions[i] + BELT_SPEED * stepScale;
            positions[i] = Math.clamp(proposed, 0, cap);
        }
    }

    @Override
    public void submit(
            BeltRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        for (int i = 0; i < renderState.items.size(); i++) {
            if (i == 0 && renderState.hideFrontItem) continue; // already handed off - the next belt now owns drawing it
            submitItem(renderState, renderState.items.get(i), poseStack, collector);
        }
        if (renderState.itemIncomingActive) {
            submitItem(renderState, renderState.itemIncoming, poseStack, collector);
        }
    }

    private void submitItem(BeltRenderState renderState, BeltRenderState.BeltItemRenderData itemRenderData,
                            PoseStack poseStack, SubmitNodeCollector collector) {
        Vec3 offset = BeltGeometry.localOffsetAt(renderState.shape, renderState.reversed, itemRenderData.position);
        offset.add(0, 0.015, 0);
        float tilt = BeltGeometry.tiltDegrees(renderState.shape, renderState.reversed);

        poseStack.pushPose();
        poseStack.translate(
                0.5 + offset.x,
                0.625 + offset.y + antiZFightingOffset(itemRenderData.position),
                0.5 + offset.z
        );
        if (tilt != 0f) {
            if (BeltGeometry.ascendsAlongZ(renderState.shape)) poseStack.mulPose(Axis.XP.rotationDegrees(tilt));
            else poseStack.mulPose(Axis.ZP.rotationDegrees(-tilt));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.scale(BeltBlockEntity.SCALE, BeltBlockEntity.SCALE, BeltBlockEntity.SCALE);

        itemRenderData.itemStackRenderState.submit(poseStack, collector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    private static float antiZFightingOffset(double position) {
        return (float) (position * Z_FIGHTING_ADJUSTMENT);
    }
}