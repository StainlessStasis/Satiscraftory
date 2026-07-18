package io.github.stainlessstasis.manifold.client.belt;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.factory_component.Belt;
import io.github.stainlessstasis.manifold.factory_component.PayloadItems;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.block_entity.BeltBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.math.Constants.EPSILON;

public class BeltRenderer implements BlockEntityRenderer<BeltBlockEntity, BeltRenderState> {
    private static final float Z_FIGHTING_ADJUSTMENT = 0.001f;

    private static final Identifier STRAIGHT_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_straight");
    private static final Identifier CURVED_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_curved");
    private static final Identifier ASCENDING_TEX = Identifier.fromNamespaceAndPath("manifold", "block/belt/belt_ascending");

    private final ItemModelResolver itemModelResolver;
    private final SpriteGetter sprites;
    private final Vector3f scratchP0 = new Vector3f();
    private final Vector3f scratchP1 = new Vector3f();
    private final Vector3f scratchP2 = new Vector3f();
    private final Vector3f scratchP3 = new Vector3f();

    public BeltRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.sprites = context.sprites();
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
        renderState.cornerSegments = BeltGeometry.segmentsForDistance(
                cameraPosition.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()))
        );

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

        double rawFront = frontRawPosition(syncedItems, elapsedTicks, speed);
        boolean frontAtEnd = rawFront >= 1d - EPSILON;

        boolean clientJammed = frontAtEnd && output != null && !hasRoomAtBack(output, partialTick);
        boolean serverJammed = blockEntity.isFrontJammed();

        boolean frontJammed = serverJammed || clientJammed;
        renderState.hideFrontItem = frontAtEnd && !frontJammed;

        updateIncomingItem(blockEntity, renderState, partialTick, predictedPositions);

        boolean jammed = !syncedItems.isEmpty() && frontJammed;
        if (blockEntity.getLevel() != null) {
            double worldElapsed = blockEntity.getLevel().getGameTime() + partialTick;

            if (!blockEntity.isScrollInitialized()) {
                blockEntity.setLastScrollGameTime(worldElapsed);
                blockEntity.setScrollInitialized(true);
            }

            if (!jammed) {
                double delta = worldElapsed - blockEntity.getLastScrollGameTime();
                blockEntity.setScrollOffset((float) wrap01(blockEntity.getScrollOffset() + delta * speed));
            }
            blockEntity.setLastScrollGameTime(worldElapsed);

            renderState.scrollOffset = blockEntity.getScrollOffset();
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
        return hasRoom(positions);
    }

    private boolean hasRoom(double[] positions) {
        return positions[positions.length - 1] >= BeltBlockEntity.MIN_GAP;
    }

    /**
     * If the upstream neighbor's front item has crossed the boundary into this belt,
     * draw it here and continue from exactly where the upstream belt says it is,
     * so it hands off smoothly
     */
    private void updateIncomingItem(BeltBlockEntity self, BeltRenderState renderState, float partialTick, double[] positions) {
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

        if (!hasRoom(positions)) return;

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

    private TextureAtlasSprite spriteFor(BeltShape shape) {
        Identifier tex = shape.isCorner() ? CURVED_TEX : (shape.isAscending() ? ASCENDING_TEX : STRAIGHT_TEX);
        return sprites.get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, tex));
    }

    private void submitBeltStrip(BeltRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector) {
        TextureAtlasSprite sprite = spriteFor(renderState.shape);
        RenderType renderType = RenderTypes.entityCutout(sprite.atlasLocation());
        List<BeltGeometry.BeltStripQuad> quads = BeltGeometry.stripQuadsFor(renderState.shape, renderState.cornerSegments);

        boolean flip = needsMirror(renderState.shape, renderState.reversed);
        double phase = renderState.reversed ? renderState.scrollOffset : (1 - renderState.scrollOffset);
        int count = quads.size();

        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            VertexConsumer wrapped = sprite.wrap(buffer);
            for (int i = 0; i < count; i++) {
                float tStart = (float) i / count;
                float tEnd = (float) (i + 1) / count;
                emitArcSegment(pose, wrapped, quads.get(i), tStart, tEnd, phase, renderState.lightCoords, flip);
            }
        });
    }

    private boolean needsMirror(BeltShape shape, boolean reversed) {
        if (reversed) {
            return shape.isCorner();
        } else {
            return shape == BeltShape.EAST_WEST || shape == BeltShape.NORTH_SOUTH;
        }
    }

    private void emitQuadSegment(PoseStack.Pose pose, VertexConsumer buffer, BeltGeometry.BeltStripQuad quad,
                                 float geomStart, float geomEnd, float vStart, float vEnd, int light) {
        Vector3f p0 = quad.pointAt(geomStart, 0, scratchP0), p1 = quad.pointAt(geomStart, 1, scratchP1);
        Vector3f p2 = quad.pointAt(geomEnd, 1, scratchP2), p3 = quad.pointAt(geomEnd, 0, scratchP3);

        buffer.addVertex(pose, p0.x, p0.y, p0.z)
                .setColor(255, 255, 255, 255).setUv(quad.u0(), vStart)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p1.x, p1.y, p1.z)
                .setColor(255, 255, 255, 255).setUv(quad.u1(), vStart)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p2.x, p2.y, p2.z)
                .setColor(255, 255, 255, 255).setUv(quad.u1(), vEnd)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        buffer.addVertex(pose, p3.x, p3.y, p3.z)
                .setColor(255, 255, 255, 255).setUv(quad.u0(), vEnd)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void emitArcSegment(PoseStack.Pose pose, VertexConsumer buffer, BeltGeometry.BeltStripQuad quad,
                                float tStart, float tEnd, double phase, int light, boolean flip) {
        float span = quad.v1() - quad.v0();
        float vStart = (float) (quad.v0() + span * wrap01(tStart + phase));
        float vEndRaw = vStart + span * (tEnd - tStart);

        if (vEndRaw <= quad.v1() + EPSILON) {
            emitQuadSegment(pose, buffer, quad, 0f, 1f, reflect(vStart, quad, flip), reflect(vEndRaw, quad, flip), light);
        } else {
            float overflow = vEndRaw - quad.v1();
            float splitT = 1f - overflow / (span * (tEnd - tStart));
            emitQuadSegment(pose, buffer, quad, 0f, splitT, reflect(vStart, quad, flip), reflect(quad.v1(), quad, flip), light);
            emitQuadSegment(pose, buffer, quad, splitT, 1f, reflect(quad.v0(), quad, flip), reflect(quad.v0() + overflow, quad, flip), light);
        }
    }

    private static float reflect(float v, BeltGeometry.BeltStripQuad quad, boolean flip) {
        return flip ? (quad.v0() + quad.v1() - v) : v;
    }

    private static double wrap01(double v) {
        double w = v % 1;
        return w < 0 ? w + 1 : w;
    }

    private static float antiZFightingOffset(double position) {
        return (float) (position * Z_FIGHTING_ADJUSTMENT);
    }
}