package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PowerProducerBlockEntity extends BlockEntity implements PowerLinkable {
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

        double supplyRate = getBlockState().getBlock() instanceof PowerProducerBlock powerProducerBlock
                ? powerProducerBlock.getSupplyRate()
                : PowerProducerBlock.DEFAULT_SUPPLY_RATE;
        powerProducer = new PowerProducer(supplyRate);

        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        PowerGrid powerGrid = FactoryNetwork.get(serverLevel).getPowerGrid();
        powerGrid.registerProducer(globalPos, powerProducer.getEffectiveSupplyRate());
        powerGrid.setMaxConnections(globalPos, getMaxPowerConnections());
    }

    public PowerProducer getPowerProducer() {
        return powerProducer;
    }
}
