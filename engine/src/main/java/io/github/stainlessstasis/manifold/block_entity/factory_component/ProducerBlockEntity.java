package io.github.stainlessstasis.manifold.block_entity.factory_component;

import io.github.stainlessstasis.manifold.factory_component.Producer;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import io.github.stainlessstasis.manifold.util.FactoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ProducerBlockEntity extends BlockEntity {
    private Producer producer;

    public ProducerBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.PRODUCER.get(), pos, state);
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
                () -> new Producer(Producer.DEFAULT_ITEM_TYPE, Producer.DEFAULT_INTERVAL_TICKS, FactoryNetwork.NO_OP_PORT, network.getScheduler()));

        relink(network);
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos pos = getBlockPos();
        BlockPos outputPos = resolveOutputPos();
        network.linkProducerOutput(
                GlobalPos.of(serverLevel.dimension(), pos),
                GlobalPos.of(serverLevel.dimension(), outputPos),
                FactoryUtils.getOutputDirection(pos, outputPos)
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

