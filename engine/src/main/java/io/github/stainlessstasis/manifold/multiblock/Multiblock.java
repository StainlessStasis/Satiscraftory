package io.github.stainlessstasis.manifold.multiblock;

import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface Multiblock<B extends BaseEntityBlock & Multiblock<B>> {
    @SuppressWarnings("unchecked")
    default B getPreviewBlock() {
        return (B) this;
    }

    MultiblockShape getMultiblockShape();
    BlockState getPreviewPlacement(BlockPlaceContext context);

    default void stampMultiblockFillers(LevelAccessor level, BlockPos controllerPos, Direction facing) {
        MultiblockPlacement.stampFillers(level, getMultiblockShape(), controllerPos, facing, ManifoldBlocks.MULTIBLOCK_FILLER.get());
    }

    default void demolishMultiblockFillers(Level level, BlockPos controllerPos, Direction facing) {
        if (MultiblockDemolition.isInProgress(level)) return;
        MultiblockDemolition.demolishFillers(level, getMultiblockShape().absoluteFillerPositions(controllerPos, facing));
    }
}
