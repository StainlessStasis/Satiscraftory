package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

public final class MultiblockPlacement {
    private MultiblockPlacement() {}

    public static boolean footprintIsClear(LevelReader level, MultiblockShape shape, BlockPos origin, Direction facing) {
        for (BlockPos pos : shape.absoluteAllPositions(origin, facing)) {
            if (!isReplaceable(level.getBlockState(pos))) return false;
        }
        return true;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    /**
     * Places filler blocks across every non-open, non-controller cell in the footprint,
     * and points each one's block entity back at the controller
     */
    public static void stampFillers(LevelAccessor level, MultiblockShape shape, BlockPos controllerPos, Direction facing, Block fillerBlock) {
        for (BlockPos pos : shape.absoluteFillerPositions(controllerPos, facing)) {
            level.setBlock(pos, fillerBlock.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof MultiblockFillerBlockEntity fillerBE) {
                fillerBE.setControllerPos(controllerPos);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canPlaceMultiblock(Level level, MultiblockShape shape, BlockPos origin, Direction facing) {
        for (BlockPos pos : shape.absoluteAllPositions(origin, facing)) {
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.canBeReplaced()) return false;
            if (!level.getWorldBorder().isWithinBounds(pos) || pos.getY() < level.getMinY() || pos.getY() >= level.getMaxY()) return false;
            if (!level.isUnobstructed(null, Shapes.block().move(pos.getX(), pos.getY(), pos.getZ()))) return false;
            if (!footprintIsClear(level, shape, origin, facing)) return false;
        }
        return true;
    }
}