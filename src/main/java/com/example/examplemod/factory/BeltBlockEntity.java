package com.example.examplemod.factory;

import com.example.examplemod.engine.Belt;
import com.example.examplemod.engine.FactoryNetwork;
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
    }

    public void relink(FactoryNetwork network) {
        network.linkBeltOutput(getBlockPos(), resolveOutputPos());
    }

    private BlockPos resolveOutputPos() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getBlockPos().relative(facing);
    }

    public Belt getBelt() {
        return belt;
    }

    public static void onBlockBroken(ServerLevel level, BlockPos pos) {
        FactoryNetwork.get(level).removeBelt(pos);
    }
}

