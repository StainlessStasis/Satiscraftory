package io.github.stainlessstasis.manifold.factory_component;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPlacement;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.util.MessageUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jspecify.annotations.NonNull;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public abstract class AbstractDirectionalFactoryBlock extends AbstractFactoryBlock {
    protected AbstractDirectionalFactoryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();

        if (this instanceof Multiblock<?> multiblock) {
            if (!multiblock.isMultiblockPlacementValid(context, facing)) {
                MultiblockShape shape = multiblock.getMultiblockShape();
                MessageUtil.warnPlayer(context, Manifold.MODID + ".invalid_multiblock_placement",
                        shape.width(), shape.depth(), shape.height());
                return null;
            }
        }

        return computeStateForPlacement(context);
    }

    public BlockState computeStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}