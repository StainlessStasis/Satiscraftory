package io.github.stainlessstasis.manifold.client.belt;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory_component.Belt;
import io.github.stainlessstasis.manifold.factory_component.PayloadItems;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.block_entity.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.mojang.math.Constants.EPSILON;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private static final float Z_FIGHTING_ADJUSTMENT = 0.001f;

    private static final Identifier STRAIGHT_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_straight");
    private static final Identifier CURVED_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_curved");
    private static final Identifier ASCENDING_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_ascending");

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
        BlockEntityRenderState.extractBase(blockEntity, renderState, crumblingOverlay);
        renderState.shape = blockEntity.getBlockState().getValue(BeltBlock.SHAPE);
        renderState.reversed = blockEntity.getBlockState().getValue(BeltBlock.REVERSED);

        double speed = blockEntity.getSpeed();
        List<Belt.ItemSnapshot> syncedItems = blockEntity.getRenderItems();
        long syncTick = blockEntity.getLastSyncedTick();

        if (syncedItems != renderState.syncedItems) {
            renderState.syncedItems = syncedItems;
            updateItemCache(blockEntity, renderState, syncedItems);
        }
        renderState.syncTick = syncTick;

        double elapsedTicks = elapsedTicksSince(blockEntity, syncTick, partialTick);
        double[] predictedPositions = predictPositions(syncedItems, elapsedTicks, speed);
        for (int i = 0; i < renderState.items.size() && i < predictedPositions.length; i++) {
            renderState.items.get(i).position = predictedPositions[i];
        }

        BeltBlockEntity input = getNeighborBeltAt(blockEntity, blockEntity.resolveInputPos());
        BeltBlockEntity output = getNeighborBeltAt(blockEntity, blockEntity.resolveOutputPos());
        renderState.neighborShapeAtStart = (input != null) ? input.getBlockState().getValue(BeltBlock.SHAPE) : null;
        renderState.neighborShapeAtEnd = (output != null) ? output.getBlockState().getValue(BeltBlock.SHAPE) : null;

        boolean outputHasRoom = output == null || hasRoomAtBack(output, partialTick);
        double rawFront = frontRawPosition(syncedItems, elapsedTicks, speed);
        renderState.hideFrontItem = outputHasRoom && rawFront >= 1d - EPSILON;

        updateIncomingItem(blockEntity, renderState, partialTick);

        if (blockEntity.getLevel() != null) {
            double worldElapsed = blockEntity.getLevel().getGameTime() + partialTick;
            renderState.scrollOffset = (float) ((worldElapsed * speed) % 1);
        }
    }

    private double frontRawPosition(List<Belt.ItemSnapshot> syncedItems, double elapsedTicks, double speed) {
        if (syncedItems.isEmpty()) return Double.NEGATIVE_INFINITY;
        return syncedItems.getFirst().position() + speed * Math.max(elapsedTicks, 0);
    }

    private double elapsedTicksSince(BeltBlockEntity belt, long syncTick, float partialTick) {
        if (belt.getLevel() == null) return 0;
        return (belt.getLevel().getGameTime() - syncTick) + partialTick;
    }

    private boolean hasRoomAtBack(BeltBlockEntity belt, float partialTick) {
        List<Belt.ItemSnapshot> synced = belt.getRenderItems();
        if (synced.isEmpty()) return true;
        double elapsed = elapsedTicksSince(belt, belt.getLastSyncedTick(), partialTick);
        double[] positions = predictPositions(synced, elapsed, belt.getSpeed());
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
        double inputRawFront = frontRawPosition(inputSynced, inputElapsed, input.getSpeed());
        if (inputRawFront < 1d - EPSILON) return;

        if (!hasRoomAtBack(self, partialTick)) return;

        double overflow = Math.clamp(inputRawFront - 1d, 0d, 1d);
        Identifier itemId = inputSynced.getFirst().itemId();
        renderState.itemIncomingActive = true;
        renderState.itemIncoming.position = overflow;
        renderState.itemIncoming.itemId = itemId;

        if (!itemId.equals(renderState.itemIncomingTypeCached)) {
            renderState.itemIncomingTypeCached = itemId;
            ItemStack stack = PayloadItems.toItemStack(itemId, 1);
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
                    (reuseIndex < oldSize && oldItems.get(reuseIndex).itemId.equals(snapshot.itemId()))
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
        itemRenderData.itemId = snapshot.itemId();

        ItemStack itemStack = PayloadItems.toItemStack(snapshot.itemId(), 1);
        if (itemStack != null) {
            itemModelResolver.updateForTopItem(itemRenderData.itemStackRenderState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }
        return itemRenderData;
    }

    private double[] predictPositions(List<Belt.ItemSnapshot> syncedItems, double elapsedTicks, double speed) {
        double[] positions = new double[syncedItems.size()];
        for (int i = 0; i < syncedItems.size(); i++) positions[i] = syncedItems.get(i).position();

        if (elapsedTicks <= 0 || positions.length == 0) return positions;

        int maxPredictedTicks = (int) Math.ceil(2d / speed);
        int fullTicks = (int) Math.min(Math.floor(elapsedTicks), maxPredictedTicks);
        double fraction = elapsedTicks - Math.floor(elapsedTicks);
        if (elapsedTicks > maxPredictedTicks) fraction = 0;

        for (int step = 0; step < fullTicks; step++) advanceOnce(positions, 1.0, speed);
        if (fraction > 0) advanceOnce(positions, fraction, speed);
        return positions;
    }

    private void advanceOnce(double[] positions, double stepScale, double speed) {
        for (int i = 0; i < positions.length; i++) {
            double cap = (i == 0) ? 1d : Math.max(positions[i - 1] - BeltBlockEntity.MIN_GAP, 0);
            double proposed = positions[i] + speed * stepScale;
            positions[i] = Math.clamp(proposed, 0, cap);
        }
    }

    @Override
    public void submit(
            @NonNull BeltRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        submitBeltStrip(renderState, poseStack, collector);

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
        offset = offset.add(0, 0.05, 0);
        float tilt = BeltGeometry.interpolatedTilt(
                renderState.shape, renderState.reversed, itemRenderData.position,
                renderState.neighborShapeAtStart, renderState.neighborShapeAtEnd
        );

        poseStack.pushPose();
        poseStack.translate(
                0.5 + offset.x,
                BeltGeometry.SURFACE_HEIGHT + offset.y + antiZFightingOffset(itemRenderData.position),
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

    private void submitBeltStrip(BeltRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        Identifier texture = textureFor(renderState.shape);
        RenderType renderType = BELT_STRIP_CUTOUT.apply(texture);

        BeltGeometry.BeltStripQuad quad = BeltGeometry.stripQuadFor(renderState.shape);
        double phase = renderState.reversed ? renderState.scrollOffset : (1 - renderState.scrollOffset);

        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) ->
                emitScrollingQuad(pose, buffer, quad, phase, renderState.lightCoords));
    }

    private Identifier textureFor(BeltShape shape) {
        return shape.isCorner() ? CURVED_TEX : (shape.isAscending() ? ASCENDING_TEX : STRAIGHT_TEX);
    }

    private void emitScrollingQuad(PoseStack.Pose pose, VertexConsumer buffer, BeltGeometry.BeltStripQuad quad, double scrollUnits, int light) {
        float v0 = (float) (quad.v0() + scrollUnits);
        float v1 = (float) (quad.v1() + scrollUnits);
        float u0 = quad.u0();
        float u1 = quad.u1();

        Vec3 p0 = quad.pointAt(0, 0), p1 = quad.pointAt(0, 1);
        Vec3 p2 = quad.pointAt(1, 1), p3 = quad.pointAt(1, 0);

        buffer.addVertex(pose, (float) p0.x, (float) p0.y, (float) p0.z)
                .setColor(255, 255, 255, 255).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, (float) p1.x, (float) p1.y, (float) p1.z)
                .setColor(255, 255, 255, 255).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, (float) p2.x, (float) p2.y, (float) p2.z)
                .setColor(255, 255, 255, 255).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, (float) p3.x, (float) p3.y, (float) p3.z)
                .setColor(255, 255, 255, 255).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private static final Function<Identifier, RenderType> BELT_STRIP_CUTOUT = Util.memoize(
            texture -> RenderType.create(
                    Manifold.id("belt_strip_cutout_" + texture.getPath().replace('/', '_')).toString(),
                    RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                            .useLightmap()
                            .withTexture("Sampler0", texture,
                                    () -> RenderSystem.getSamplerCache()
                                            .getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.NEAREST, FilterMode.NEAREST, false))
                            .affectsCrumbling()
                            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                            .createRenderSetup()
            )
    );

    private static float antiZFightingOffset(double position) {
        return (float) (position * Z_FIGHTING_ADJUSTMENT);
    }
}