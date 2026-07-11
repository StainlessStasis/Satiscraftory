package com.example.examplemod.engine_internal.block.belt;

import com.example.examplemod.engine_internal.block.AbstractFactoryBlock;
import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.example.examplemod.engine_internal.factory.FactoryLinking;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BeltBlock extends AbstractFactoryBlock {
    public static final EnumProperty<BeltShape> SHAPE = EnumProperty.create("shape", BeltShape.class);
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");

    private static final MapCodec<BeltBlock> CODEC = simpleCodec(BeltBlock::new);

    public BeltBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SHAPE, BeltShape.NORTH_SOUTH).setValue(REVERSED, false));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, REVERSED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BeltShape shape = BeltShapeSolver.computeShapeForPlacement(level, pos, context);
        boolean reversed = BeltShapeSolver.computeReversedForPlacement(level, pos, shape, context);
        return defaultBlockState().setValue(SHAPE, shape).setValue(REVERSED, reversed);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BeltBlockEntity(InternalEngineBlockEntities.BELT.get(), pos, state);
    }

    /**
     * Click to flip which end is input vs output
     */
    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel) {
            boolean reversed = state.getValue(REVERSED);
            serverLevel.setBlock(pos, state.setValue(REVERSED, !reversed), Block.UPDATE_ALL);
            if (serverLevel.getBlockEntity(pos) instanceof BeltBlockEntity beltBE) {
                beltBE.relink(FactoryNetwork.get(serverLevel));
            }
            FactoryLinking.relinkNeighbors(serverLevel, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof BeltBlockEntity beltBE) beltBE.onNeighborChanged();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeBelt(GlobalPos.of(level.dimension(), pos));
    }
}