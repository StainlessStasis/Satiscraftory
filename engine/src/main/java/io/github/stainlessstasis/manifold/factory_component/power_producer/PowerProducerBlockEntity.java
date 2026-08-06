package io.github.stainlessstasis.manifold.factory_component.power_producer;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.PowerProducingFactoryBlockEntity;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PowerProducerBlockEntity extends PowerProducingFactoryBlockEntity<PowerProducer> {
    private PowerProducer powerProducer;

    public PowerProducerBlockEntity(BlockPos pos, BlockState state) {
        this(ManifoldBlockEntities.POWER_PRODUCER.get(), pos, state);
    }

    public PowerProducerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());

        powerProducer = network.getOrCreatePowerProducer(globalPos, () -> {
            double supplyRate = getBlockState().getBlock() instanceof PowerProducerBlock powerProducerBlock
                    ? powerProducerBlock.getSupplyRate()
                    : PowerProducerBlock.DEFAULT_SUPPLY_RATE;
            return new PowerProducer(supplyRate);
        });

        registerPowerProducer(serverLevel);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PowerProducerBlockEntity producerBE) {
        producerBE.updatePowerSupply(level);
    }

    public PowerProducer getPowerProducer() {
        return powerProducer;
    }

    @Override
    public PowerProducer getFactoryComponent() {
        return powerProducer;
    }
}