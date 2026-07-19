package io.github.stainlessstasis.manifold.block_entity;

import io.github.stainlessstasis.manifold.factory_component.BeltLane;
import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.block.belt.BeltShapeSolver;
import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import io.github.stainlessstasis.manifold.util.FactoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BeltBlockEntity extends BlockEntity {
    public static final float SCALE = 0.5f;
    public static final double MIN_GAP = SCALE + 0.001;

    // rendering stuff
    private List<BeltLane.ItemSnapshot> previousSyncedItems = List.of();
    private long previousSyncTick = 0;
    private List<BeltLane.ItemSnapshot> currentSyncedItems = List.of();
    private long currentSyncTick = 0;
    private int ticksSinceSync = 0;
    private boolean frontJammed;
    private float baseScrollOffset;
    // only populated on the lane's anchor block; see BeltRenderer
    private List<BlockPos> syncedLaneBlocks = List.of();

    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.BELT.get(), pos, state);
    }

    public BeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        relink(FactoryNetwork.get(serverLevel));
        reconcileOwnOrientation(serverLevel);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
        notifyConnectedNeighborsToReshape(serverLevel);
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos pos = getBlockPos();
        BlockPos inputPos = resolveInputPos();
        BlockPos outputPos = resolveOutputPos();

        GlobalPos gPos = GlobalPos.of(serverLevel.dimension(), pos);
        GlobalPos gIn = GlobalPos.of(serverLevel.dimension(), inputPos);
        GlobalPos gOut = GlobalPos.of(serverLevel.dimension(), outputPos);

        network.attachBeltBlock(gPos, getSpeed(), MIN_GAP, gIn, gOut);
        network.linkLaneOutput(gPos, gOut, FactoryUtils.getOutputDirection(pos, outputPos));
    }

    public void onNeighborChanged() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockState state = getBlockState();
        BeltShape currentShape = state.getValue(BeltBlock.SHAPE);
        boolean shapeChanged = false;

        if (!currentShape.isCorner()) {
            BeltShape recomputedShape = BeltShapeSolver.computeShapeForExisting(serverLevel, getBlockPos(), currentShape);
            if (recomputedShape != currentShape) {
                boolean currentReversed = state.getValue(BeltBlock.REVERSED);
                boolean newReversed = BeltShapeSolver.preserveRoleAcrossReshape(currentShape, currentReversed, recomputedShape);
                serverLevel.setBlock(getBlockPos(),
                        state.setValue(BeltBlock.SHAPE, recomputedShape).setValue(BeltBlock.REVERSED, newReversed), Block.UPDATE_ALL);
                reconcileDownstreamOrientation(serverLevel, recomputedShape, newReversed);
                shapeChanged = true;
            }
        }

        relink(FactoryNetwork.get(serverLevel));
        reconcileOwnOrientation(serverLevel);

        if (shapeChanged) {
            notifyConnectedNeighborsToReshape(serverLevel);
        }
    }

    private void notifyConnectedNeighborsToReshape(ServerLevel level) {
        BeltShape shape = getBlockState().getValue(BeltBlock.SHAPE);

        notifyNeighborBeltAt(level, BeltShapeSolver.resolveConnectionPoint(level, getBlockPos(), shape.endADirection(), shape.endAYOffset()));
        notifyNeighborBeltAt(level, BeltShapeSolver.resolveConnectionPoint(level, getBlockPos(), shape.endBDirection(), shape.endBYOffset()));

        if (shape.endAYOffset() == 0) notifyPotentialLowerAscent(level, shape.endADirection());
        if (shape.endBYOffset() == 0) notifyPotentialLowerAscent(level, shape.endBDirection());
    }

    private void notifyPotentialLowerAscent(ServerLevel level, Direction flatEndDirection) {
        notifyNeighborBeltAt(level, getBlockPos().relative(flatEndDirection).below());
    }

    private void notifyNeighborBeltAt(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BeltBlockEntity neighborBelt) {
            neighborBelt.onNeighborChanged();
        }
    }

    private void reconcileOwnOrientation(ServerLevel level) {
        BlockState state = getBlockState();
        BeltShape shape = state.getValue(BeltBlock.SHAPE);
        boolean reversed = state.getValue(BeltBlock.REVERSED);

        Boolean fromA = BeltShapeSolver.feedsRelationship(level, getBlockPos(), shape.endADirection(), shape.endAYOffset());
        Boolean fromB = BeltShapeSolver.feedsRelationship(level, getBlockPos(), shape.endBDirection(), shape.endBYOffset());

        Boolean desiredReversed = null;
        if (Boolean.TRUE.equals(fromA)) desiredReversed = false;
        else if (Boolean.FALSE.equals(fromA)) desiredReversed = true;
        else if (Boolean.TRUE.equals(fromB)) desiredReversed = true;
        else if (Boolean.FALSE.equals(fromB)) desiredReversed = false;

        if (desiredReversed != null && desiredReversed != reversed) {
            level.setBlock(getBlockPos(), state.setValue(BeltBlock.REVERSED, desiredReversed), Block.UPDATE_ALL);
            relink(FactoryNetwork.get(level));
        }
    }

    /**
     * Updates the downstream belt to prevent an accidental loop between two belts.
     * If the neighbor was placed first and faces this belt, flip it forward to ensure the belt line continues
     */
    private void reconcileDownstreamOrientation(ServerLevel level, BeltShape newShape, boolean newReversed) {
        Direction outputDir = newReversed ? newShape.defaultInputDirection() : newShape.defaultOutputDirection();
        int outputYOffset = newReversed ? newShape.defaultInputYOffset() : newShape.defaultOutputYOffset();
        BlockPos outputPos = getBlockPos().relative(outputDir).above(outputYOffset);

        if (!(level.getBlockEntity(outputPos) instanceof BeltBlockEntity downstream)) return;
        BlockState downstreamState = downstream.getBlockState();
        if (downstreamState.getValue(BeltBlock.SHAPE).isCorner()) return;

        if (getBlockPos().equals(downstream.resolveOutputPos())) {
            boolean downstreamReversed = downstreamState.getValue(BeltBlock.REVERSED);
            level.setBlock(outputPos, downstreamState.setValue(BeltBlock.REVERSED, !downstreamReversed), Block.UPDATE_ALL);
            downstream.relink(FactoryNetwork.get(level));
        }
    }

    public BlockPos resolveOutputPos() {
        return resolvePosForIO(false);
    }

    public BlockPos resolveInputPos() {
        return resolvePosForIO(true);
    }

    protected BlockPos resolvePosForIO(boolean input) {
        BeltShape shape = getBlockState().getValue(BeltBlock.SHAPE);
        boolean reversed = getBlockState().getValue(BeltBlock.REVERSED);

        Direction dir;
        if (input) dir = reversed ? shape.defaultOutputDirection() : shape.defaultInputDirection();
        else dir = reversed ? shape.defaultInputDirection() : shape.defaultOutputDirection();

        int yOffset;
        if (input) yOffset = reversed ? shape.defaultOutputYOffset() : shape.defaultInputYOffset();
        else yOffset = reversed ? shape.defaultInputYOffset() : shape.defaultOutputYOffset();

        return (level != null)
                ? BeltShapeSolver.resolveConnectionPoint(level, getBlockPos(), dir, yOffset)
                : getBlockPos().relative(dir).above(yOffset);
    }

    public @Nullable BeltLane getLane(FactoryNetwork network) {
        if (level == null) return null;
        return network.getLaneManager().laneAt(GlobalPos.of(level.dimension(), getBlockPos()));
    }

    public double getSpeed() {
        return (getBlockState().getBlock() instanceof BeltBlock beltBlock) ? beltBlock.getSpeed() : 0.05;
        // 0.05 = 1/20 (same as mk 1 speed in Satiscraftory)
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isFrontJammed() {
        return frontJammed;
    }

    // RENDERING/CLIENT
    public void clientTick(double speed) {
        ticksSinceSync++;
        if (!this.isFrontJammed() && this.level != null) {
            double worldElapsed = this.level.getGameTime();
            this.baseScrollOffset = (float) ((worldElapsed * speed) % 1.0);
        }
    }

    public List<BeltLane.ItemSnapshot> getCurrentSyncedItems() {
        return currentSyncedItems;
    }
    public long getCurrentSyncTick() {
        return currentSyncTick;
    }
    public List<BeltLane.ItemSnapshot> getPreviousSyncedItems() {
        return previousSyncedItems;
    }
    public long getPreviousSyncTick() {
        return previousSyncTick;
    }
    public int getTicksSinceSync() {
        return ticksSinceSync;
    }

    public float getBaseScrollOffset() { return baseScrollOffset; }

    /**
     * Ordered chain of blocks in this belt's lane, as of the last sync. Only populated on the anchor block
     */
    public List<BlockPos> getSyncedLaneBlocks() {
        return syncedLaneBlocks;
    }

    public void applySync(List<BlockPos> laneBlocks, List<BeltLane.ItemSnapshot> items, long syncTick, boolean frontJammed) {
        this.previousSyncedItems = this.currentSyncedItems;
        this.previousSyncTick = this.currentSyncTick;
        this.currentSyncedItems = items;
        this.currentSyncTick = syncTick;
        this.frontJammed = frontJammed;
        this.ticksSinceSync = 0;
        this.syncedLaneBlocks = laneBlocks;
    }
}