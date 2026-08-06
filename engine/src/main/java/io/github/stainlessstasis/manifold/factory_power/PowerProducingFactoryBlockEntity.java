package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.FactoryBlockEntity;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import io.github.stainlessstasis.manifold.factory_power.network.PowerLinkable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;


public abstract class PowerProducingFactoryBlockEntity<T extends PowerProducingFactoryComponent> extends FactoryBlockEntity<T> implements PowerLinkable {
    protected PowerProducingFactoryBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public void registerPowerProducer(ServerLevel level) {
        T component = getFactoryComponent();
        if (component == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), getBlockPos());
        PowerGrid powerGrid = FactoryNetwork.get(level).getPowerGrid();
        powerGrid.registerProducer(globalPos, component.getSupplyRate());
        powerGrid.setMaxConnections(globalPos, getMaxPowerConnections());
    }

    public void updatePowerSupply(ServerLevel level) {
        T component = getFactoryComponent();
        if (component == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), getBlockPos());
        FactoryNetwork.get(level).getPowerGrid().registerProducer(globalPos, component.getSupplyRate());
    }
}