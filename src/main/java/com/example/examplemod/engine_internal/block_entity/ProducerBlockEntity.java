package com.example.examplemod.engine_internal.block_entity;

import com.example.examplemod.engine_internal.PayloadItems;
import com.example.examplemod.engine_internal.Producer;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import com.example.examplemod.engine_internal.registry.InternalEngineBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ProducerBlockEntity extends BlockEntity {
    private static final String ITEM_TYPE = PayloadItems.typeIdOf(Items.RAW_IRON);
    private static final long INTERVAL_TICKS = 10;

    private Producer producer;

    public ProducerBlockEntity(BlockPos pos, BlockState state) {
        super(InternalEngineBlockEntities.PRODUCER.get(), pos, state);
    }

    public ProducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        producer = network.getOrCreateProducer(GlobalPos.of(serverLevel.dimension(), getBlockPos()),
                () -> new Producer(ITEM_TYPE, INTERVAL_TICKS, FactoryNetwork.NO_OP_PORT, network.getScheduler()));

        relink(network);
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        network.linkProducerOutput(
                GlobalPos.of(serverLevel.dimension(), getBlockPos()),
                GlobalPos.of(serverLevel.dimension(), resolveOutputPos())
        );
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
}

