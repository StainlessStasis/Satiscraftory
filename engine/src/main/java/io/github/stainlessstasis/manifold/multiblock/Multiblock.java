package io.github.stainlessstasis.manifold.multiblock;

import io.github.stainlessstasis.manifold.factory_component.AbstractDirectionalFactoryBlock;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface Multiblock<B extends BaseEntityBlock & Multiblock<B>> {
    MultiblockShape getMultiblockShape();

    @SuppressWarnings("unchecked")
    default B getPreviewBlock() {
        return (B) this;
    }

    default boolean isMultiblockPlacementValid(BlockPlaceContext context, Direction facing) {
        return MultiblockPlacement.canPlaceMultiblock(context.getLevel(), getMultiblockShape(), context.getClickedPos(), facing);
    }

    default BlockState getPreviewPlacement(BlockPlaceContext context) {
        B block = getPreviewBlock();
        if (block instanceof AbstractDirectionalFactoryBlock factoryBlock) {
            return factoryBlock.computeStateForPlacement(context);
        }
        return block.getStateForPlacement(context);
    }

    default void stampMultiblockFillers(LevelAccessor level, BlockPos controllerPos, Direction facing) {
        MultiblockPlacement.stampFillers(level, getMultiblockShape(), controllerPos, facing, ManifoldBlocks.MULTIBLOCK_FILLER.get());
    }

    default void demolishMultiblockFillers(Level level, BlockPos controllerPos, Direction facing) {
        if (MultiblockDemolition.isInProgress(level)) return;
        MultiblockDemolition.demolishFillers(level, getMultiblockShape().absoluteFillerPositions(controllerPos, facing));
    }
}
