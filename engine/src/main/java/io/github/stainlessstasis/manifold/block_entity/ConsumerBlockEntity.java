package io.github.stainlessstasis.manifold.block_entity;

import io.github.stainlessstasis.manifold.factory_component.Consumer;
import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ConsumerBlockEntity extends BlockEntity {
    private static final int CAPACITY = 8;
    private static final int PROCESS_TIME_TICKS = 0;

    private Consumer consumer;

    public ConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.CONSUMER.get(), pos, state);
    }

    public ConsumerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        consumer = network.getOrCreateConsumer(GlobalPos.of(serverLevel.dimension(), getBlockPos()), () -> new Consumer(CAPACITY, PROCESS_TIME_TICKS));

        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public Consumer getConsumer() {
        return consumer;
    }
}

