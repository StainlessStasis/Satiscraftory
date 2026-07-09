package com.example.examplemod.block;

import com.example.examplemod.engine.Producer;
import com.example.examplemod.engine.FactoryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ProducerBlockEntity extends BlockEntity {
    private static final String ITEM_TYPE = Items.RAW_IRON.toString();
    private static final long INTERVAL_TICKS = 100;

    private Producer producer;

    public ProducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        producer = network.getOrCreateProducer(getBlockPos(),
                () -> new Producer(ITEM_TYPE, INTERVAL_TICKS, FactoryNetwork.NO_OP_PORT, network.getScheduler()));

        relink(network);
    }

    public void relink(FactoryNetwork network) {
        network.linkProducerOutput(getBlockPos(), resolveOutputPos());
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

    public Producer getProducer() {
        return producer;
    }

    public static void onBlockBroken(ServerLevel level, BlockPos pos) {
        FactoryNetwork.get(level).removeProducer(pos);
    }
}

