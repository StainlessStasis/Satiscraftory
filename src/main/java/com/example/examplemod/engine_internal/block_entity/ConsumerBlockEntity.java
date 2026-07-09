package com.example.examplemod.engine_internal.block_entity;

import com.example.examplemod.engine_internal.Consumer;
import com.example.examplemod.engine_internal.factory.FactoryLinking;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ConsumerBlockEntity extends BlockEntity {
    private static final int CAPACITY = 8;
    private static final int PROCESS_TIME_TICKS = 1;

    private Consumer consumer;

    public ConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(InternalEngineBlockEntities.CONSUMER.get(), pos, state);
    }

    public ConsumerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        consumer = network.getOrCreateConsumer(getBlockPos(), () -> new Consumer(CAPACITY, PROCESS_TIME_TICKS));

        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public Consumer getConsumer() {
        return consumer;
    }
}

