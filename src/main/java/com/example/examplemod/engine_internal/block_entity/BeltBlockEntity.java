package com.example.examplemod.engine_internal.block_entity;

import com.example.examplemod.engine_internal.Belt;
import com.example.examplemod.engine_internal.factory.FactoryLinking;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BeltBlockEntity extends BlockEntity {
    private static final int LENGTH_TICKS = 10;
    private static final double MIN_GAP = 0.15;

    private Belt belt;

    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(InternalEngineBlockEntities.BELT.get(), pos, state);
    }

    public BeltBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        belt = network.getOrCreateBelt(getBlockPos(), () -> new Belt(LENGTH_TICKS, MIN_GAP));

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        network.linkBeltOutput(getBlockPos(), resolveOutputPos());
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    private BlockPos resolveOutputPos() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getBlockPos().relative(facing);
    }

    public Belt getBelt() {
        return belt;
    }
}

