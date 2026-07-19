package io.github.stainlessstasis.manifold.client.belt;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.stainlessstasis.manifold.factory_component.BeltLane;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        BlockState ownState = blockEntity.getBlockState();
        renderState.anchorPos = blockEntity.getBlockPos();
        renderState.shape = ownState.getValue(BeltBlock.SHAPE);
        renderState.reversed = ownState.getValue(BeltBlock.REVERSED);
        renderState.cornerSegments = BeltGeometry.segmentsForDistance(
                cameraPosition.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()))
        );

        float baseOffset = blockEntity.getBaseScrollOffset();
        if (!blockEntity.isFrontJammed()) {
            baseOffset += (float) (blockEntity.getSpeed() * partialTick);
        }
        renderState.scrollOffset = (float) wrap01(baseOffset);

        List<BlockPos> laneBlocks = blockEntity.getSyncedLaneBlocks();
        renderState.isAnchor = !laneBlocks.isEmpty() && laneBlocks.getFirst().equals(blockEntity.getBlockPos());

        if (!renderState.isAnchor) {
            renderState.items.clear();
            renderState.cachedSyncedItems = null;
            return;
        }

        int laneSize = laneBlocks.size();
        BeltShape[] shapes = new BeltShape[laneSize];
        boolean[] reversedFlags = new boolean[laneSize];
        Level level = blockEntity.getLevel();
        for (int i = 0; i < laneSize; i++) {
            BlockState state = (level != null) ? level.getBlockState(laneBlocks.get(i)) : ownState;
            if (!state.hasProperty(BeltBlock.SHAPE)) {
                renderState.items.clear();
                renderState.cachedSyncedItems = null;
                return;
            }
            shapes[i] = state.getValue(BeltBlock.SHAPE);
            reversedFlags[i] = state.getValue(BeltBlock.REVERSED);
        }

        renderState.laneBlocks = laneBlocks;
        renderState.blockShapes = shapes;
        renderState.laneReversed = reversedFlags;
        renderState.totalLength = laneSize;

        List<BeltLane.ItemSnapshot> currentItems = blockEntity.getCurrentSyncedItems();
        double[] rawPositions = extrapolateItemsPositions(blockEntity, partialTick, laneSize);
        double[] clampedPositions = clampPositions(rawPositions, laneSize);

        if (currentItems != renderState.cachedSyncedItems) {
            rebuildItemCache(blockEntity, renderState, currentItems);
            renderState.cachedSyncedItems = currentItems;
        }
        applyPositions(renderState, clampedPositions);
    }

    private double[] getItemVelocities(BeltBlockEntity belt, double totalLength) {
        List<BeltLane.ItemSnapshot> current = belt.getCurrentSyncedItems();
        List<BeltLane.ItemSnapshot> previous = belt.getPreviousSyncedItems();
        long deltaTick = belt.getCurrentSyncTick() - belt.getPreviousSyncTick();

        double[] velocities = new double[current.size()];
        for (int i = 0; i < current.size(); i++) {
            BeltLane.ItemSnapshot snapshot = current.get(i);
            BeltLane.ItemSnapshot matched = findById(previous, snapshot.id());
            velocities[i] = (matched != null && deltaTick > 0)
                    ? (snapshot.position() - matched.position()) / deltaTick
                    : belt.getSpeed();
        }
        return velocities;
    }

    private BeltLane.@Nullable ItemSnapshot findById(List<BeltLane.ItemSnapshot> items, long id) {
        for (BeltLane.ItemSnapshot snapshot : items) {
            if (snapshot.id() == id) return snapshot;
        }
        return null;
    }

    private double[] extrapolateItemsPositions(BeltBlockEntity belt, float partialTick, double totalLength) {
        List<BeltLane.ItemSnapshot> current = belt.getCurrentSyncedItems();
        double elapsed = Math.max(elapsedTicksSince(belt, partialTick), 0);
        double[] velocities = getItemVelocities(belt, totalLength);

        double[] positions = new double[current.size()];
        for (int i = 0; i < current.size(); i++) {
            positions[i] = current.get(i).position() + velocities[i] * elapsed;
        }
        return positions;
    }

    private double[] clampPositions(double[] raw, double totalLength) {
        double[] clamped = new double[raw.length];
        double previousClamped = totalLength;
        for (int i = 0; i < raw.length; i++) {
            double cap = (i == 0) ? totalLength : Math.max(previousClamped - BeltBlockEntity.MIN_GAP, 0);
            previousClamped = Math.clamp(raw[i], 0, cap);
            clamped[i] = previousClamped;
        }
        return clamped;
    }

    private double elapsedTicksSince(BeltBlockEntity belt, float partialTick) {
        return belt.getTicksSinceSync() + partialTick;
    }

    private void rebuildItemCache(BeltBlockEntity blockEntity, BeltRenderState renderState, List<BeltLane.ItemSnapshot> currentItems) {
        Map<Long, BeltRenderState.BeltItemRenderData> reusable = new HashMap<>();
        for (BeltRenderState.BeltItemRenderData data : renderState.items) reusable.put(data.id, data);

        List<BeltRenderState.BeltItemRenderData> newItems = new ArrayList<>(currentItems.size());
        for (BeltLane.ItemSnapshot snapshot : currentItems) {
            BeltRenderState.BeltItemRenderData data = reusable.get(snapshot.id());
            if (data == null) {
                data = buildItemRenderData(blockEntity, snapshot);
            }
            newItems.add(data);
        }

        renderState.items.clear();
        renderState.items.addAll(newItems);
    }

    private void applyPositions(BeltRenderState renderState, double[] positions) {
        for (int i = 0; i < renderState.items.size() && i < positions.length; i++) {
            renderState.items.get(i).position = positions[i];
        }
    }

    private BeltRenderState.BeltItemRenderData buildItemRenderData(BeltBlockEntity blockEntity, BeltLane.ItemSnapshot snapshot) {
        BeltRenderState.BeltItemRenderData itemRenderData = new BeltRenderState.BeltItemRenderData();
        itemRenderData.id = snapshot.id();
        itemRenderData.position = snapshot.position();
        itemRenderData.itemId = snapshot.itemId();

        ItemStack itemStack = PayloadItems.toItemStack(snapshot.itemId(), 1);
        if (itemStack != null) {
            itemModelResolver.updateForTopItem(itemRenderData.itemStackRenderState, itemStack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
        }
        return itemRenderData;
    }

    @Override
    public void submit(
            @NonNull BeltRenderState renderState, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraRenderState
    ) {
        submitBeltStrip(renderState, poseStack, collector);

        // only the lane's anchor block renders items for the entire lane
        if (!renderState.isAnchor) return;
        int laneSize = renderState.blockShapes.length;
        if (laneSize == 0) return;

        for (BeltRenderState.BeltItemRenderData item : renderState.items) {
            submitItemInLane(renderState, item, poseStack, collector);
        }
    }

    private void submitItemInLane(BeltRenderState renderState, BeltRenderState.BeltItemRenderData itemRenderData,
                                  PoseStack poseStack, SubmitNodeCollector collector) {
        int laneSize = renderState.blockShapes.length;
        int blockIndex = (int) Math.min(Math.floor(itemRenderData.position), laneSize - 1);
        double localT = Math.clamp(itemRenderData.position - blockIndex, 0, 1);

        BeltShape shape = renderState.blockShapes[blockIndex];
        boolean reversed = renderState.laneReversed[blockIndex];
        BeltShape neighborBefore = (blockIndex > 0) ? renderState.blockShapes[blockIndex - 1] : null;
        BeltShape neighborAfter = (blockIndex < laneSize - 1) ? renderState.blockShapes[blockIndex + 1] : null;

        Vec3 offset = BeltGeometry.localOffsetAt(shape, reversed, localT);
        offset = offset.add(0, 0.05, 0);
        float tilt = BeltGeometry.interpolatedTilt(shape, reversed, localT, neighborBefore, neighborAfter);

        BlockPos delta = renderState.laneBlocks.get(blockIndex).subtract(renderState.anchorPos);

        poseStack.pushPose();
        poseStack.translate(
                delta.getX() + 0.5 + offset.x,
                delta.getY() + BeltGeometry.SURFACE_HEIGHT + offset.y + antiZFightingOffset(localT),
                delta.getZ() + 0.5 + offset.z
        );
        if (tilt != 0f) {
            if (BeltGeometry.ascendsAlongZ(shape)) poseStack.mulPose(Axis.XP.rotationDegrees(tilt));
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

        collector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            VertexConsumer wrapped = sprite.wrap(vertexConsumer);
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

    private static float antiZFightingOffset(double localT) {
        return (float) (localT * Z_FIGHTING_ADJUSTMENT);
    }
}