package io.github.stainlessstasis.manifold.factory_component.belt;

import io.github.stainlessstasis.manifold.factory_component.consumer.ConsumerBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.machine.MachineBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class BeltShapeSolver {
    public static BeltShape computeShapeForPlacement(Level level, BlockPos pos, BlockPlaceContext context) {
        Direction playerFacing = context.getHorizontalDirection().getOpposite();
        return computeShape(level, pos, playerFacing);
    }

    public static BeltShape computeShapeForExisting(Level level, BlockPos pos, BeltShape currentShape) {
        return computeShape(level, pos, currentShape.defaultOutputDirection());
    }

    public static boolean computeReversed(Level level, BlockPos pos, BeltShape shape) {
        Boolean fromA = feedsRelationship(level, pos, shape.endADirection(), shape.endAYOffset());
        Boolean fromB = feedsRelationship(level, pos, shape.endBDirection(), shape.endBYOffset());

        if (Boolean.TRUE.equals(fromA)) return false;
        if (Boolean.FALSE.equals(fromA)) return true;
        return Boolean.TRUE.equals(fromB);
    }

    public static boolean computeReversedForPlacement(Level level, BlockPos pos, BeltShape shape, BlockPlaceContext context) {
        Boolean fromA = feedsRelationship(level, pos, shape.endADirection(), shape.endAYOffset());
        Boolean fromB = feedsRelationship(level, pos, shape.endBDirection(), shape.endBYOffset());

        if (Boolean.TRUE.equals(fromA)) return false;
        if (Boolean.FALSE.equals(fromA)) return true;
        if (Boolean.TRUE.equals(fromB)) return true;
        if (Boolean.FALSE.equals(fromB)) return false;

        Direction playerFacing = context.getHorizontalDirection().getOpposite();
        return shape.defaultOutputDirection() != playerFacing;
    }

    /**
     *  When bending an existing straight/sloped belt into a corner,
     *  the corner shares exactly one end with the old shape.
     *  Find that shared direction and keep its existing input/output role rather than recalculating it
     */
    public static boolean preserveRoleAcrossReshape(BeltShape oldShape, boolean oldReversed, BeltShape newShape) {
        Direction shared = sharedDirection(oldShape, newShape);
        if (shared == null) return false;

        Direction oldInputDirection = oldReversed ? oldShape.defaultOutputDirection() : oldShape.defaultInputDirection();
        boolean sharedWasInput = shared == oldInputDirection;
        boolean sharedIsInputByDefault = shared == newShape.defaultInputDirection();
        return sharedWasInput != sharedIsInputByDefault;
    }

    private static Direction sharedDirection(BeltShape oldShape, BeltShape newShape) {
        if (newShape.connectsTo(oldShape.endADirection())) return oldShape.endADirection();
        if (newShape.connectsTo(oldShape.endBDirection())) return oldShape.endBDirection();
        return null;
    }

    /**
     * true = neighbor outputs into this, false = it expects input from this, null = no signal
     */
    public static @Nullable Boolean feedsRelationship(Level level, BlockPos pos, Direction dir, int yOffset) {
        BeltBlockEntity beltBE = findConnectingBelt(level, pos, dir, yOffset);
        if (beltBE != null) {
            if (pos.equals(beltBE.resolveOutputPos())) return true;
            if (pos.equals(beltBE.resolveInputPos())) return false;
            return null;
        }

        BlockPos neighborPos = pos.relative(dir).above(yOffset);
        BlockEntity blockEntity = level.getBlockEntity(neighborPos);
        if (blockEntity instanceof ProducerBlockEntity || blockEntity instanceof MachineBlockEntity) {
            // producers/machines only ever push out through their own facing,
            // so they can only ever feed a neighbor, never be fed
            Direction facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            return pos.equals(neighborPos.relative(facing)) ? true : null;
        }
        if (blockEntity instanceof ConsumerBlockEntity) {
            return false;
        }
        return null;
    }

    private static BeltShape computeShape(Level level, BlockPos pos, Direction fallbackFacing) {
        boolean north = hasFlatConnection(level, pos, Direction.NORTH);
        boolean south = hasFlatConnection(level, pos, Direction.SOUTH);
        boolean east = hasFlatConnection(level, pos, Direction.EAST);
        boolean west = hasFlatConnection(level, pos, Direction.WEST);

        if (!(north && south) && !(east && west)) {
            if (north && east) return BeltShape.NORTH_EAST;
            if (north && west) return BeltShape.NORTH_WEST;
            if (south && east) return BeltShape.SOUTH_EAST;
            if (south && west) return BeltShape.SOUTH_WEST;
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (hasHigherNeighbor(level, pos, dir)) {
                return switch (dir) {
                    case NORTH -> BeltShape.ASCENDING_NORTH;
                    case SOUTH -> BeltShape.ASCENDING_SOUTH;
                    case EAST -> BeltShape.ASCENDING_EAST;
                    case WEST -> BeltShape.ASCENDING_WEST;
                    default -> straightFor(fallbackFacing);
                };
            }
        }

        return straightFor(fallbackFacing);
    }

    private static BeltShape straightFor(Direction facing) {
        return (facing.getAxis() == Direction.Axis.Z) ? BeltShape.NORTH_SOUTH : BeltShape.EAST_WEST;
    }

    private static boolean hasFlatConnection(Level level, BlockPos pos, Direction dir) {
        return findConnectingBelt(level, pos, dir, 0) != null;
    }

    private static boolean hasHigherNeighbor(Level level, BlockPos pos, Direction dir) {
        BlockPos higherPos = pos.relative(dir).above(1);
        BeltBlockEntity higherBelt = beltAt(level, higherPos);
        if (higherBelt == null) return false;
        Integer endOffset = endYOffsetTowards(higherBelt.getBlockState().getValue(BeltBlock.SHAPE), dir.getOpposite());
        return endOffset != null && endOffset == 0;
    }

    private static @Nullable BeltBlockEntity findConnectingBelt(Level level, BlockPos pos, Direction dir, int yOffset) {
        BlockPos samePos = pos.relative(dir).above(yOffset);
        BeltBlockEntity sameBelt = beltAt(level, samePos);
        if (sameBelt != null) {
            Integer endOffset = endYOffsetTowards(sameBelt.getBlockState().getValue(BeltBlock.SHAPE), dir.getOpposite());
            if (endOffset != null && endOffset == 0) return sameBelt;
        }

        if (yOffset == 0) {
            BlockPos lowerPos = samePos.below();
            BeltBlockEntity lowerBelt = beltAt(level, lowerPos);
            if (lowerBelt != null) {
                Integer endOffset = endYOffsetTowards(lowerBelt.getBlockState().getValue(BeltBlock.SHAPE), dir.getOpposite());
                if (endOffset != null && endOffset == 1) return lowerBelt;
            }
        }
        return null;
    }

    public static BlockPos resolveConnectionPoint(Level level, BlockPos pos, Direction dir, int yOffset) {
        BlockPos samePos = pos.relative(dir).above(yOffset);
        if (yOffset != 0) return samePos;
        if (beltAt(level, samePos) != null) return samePos;

        BlockPos lowerPos = samePos.below();
        BeltBlockEntity lowerBelt = beltAt(level, lowerPos);
        if (lowerBelt != null) {
            Integer endOffset = endYOffsetTowards(lowerBelt.getBlockState().getValue(BeltBlock.SHAPE), dir.getOpposite());
            if (endOffset != null && endOffset == 1) return lowerPos;
        }
        return samePos;
    }

    private static @Nullable BeltBlockEntity beltAt(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return (blockEntity instanceof BeltBlockEntity beltBE && beltBE.getBlockState().getBlock() instanceof BeltBlock) ? beltBE : null;
    }

    private static @Nullable Integer endYOffsetTowards(BeltShape shape, Direction direction) {
        if (shape.endADirection() == direction) return shape.endAYOffset();
        if (shape.endBDirection() == direction) return shape.endBYOffset();
        return null;
    }
}