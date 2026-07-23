package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MultiblockPlacement {
    private MultiblockPlacement() {}

    public static boolean footprintIsClear(LevelReader level, MultiblockShape shape, BlockPos controllerPos, Direction facing) {
        for (BlockPos pos : shape.absoluteAllPositions(controllerPos, facing)) {
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
}