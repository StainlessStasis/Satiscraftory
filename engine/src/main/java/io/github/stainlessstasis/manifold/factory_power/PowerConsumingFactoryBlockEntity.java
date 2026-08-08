package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import io.github.stainlessstasis.manifold.factory_power.network.PowerLinkable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PowerConsumingFactoryBlockEntity<T extends PowerConsumingFactoryComponent> extends PowerableFactoryBlockEntity<T> implements PowerLinkable {
    public PowerConsumingFactoryBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public abstract double getPowerDemand();

    public void registerPowerConsumer(ServerLevel level) {
        T factoryComponent = getFactoryComponent();
        if (factoryComponent == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), getBlockPos());
        PowerGrid powerGrid = FactoryNetwork.get(level).getPowerGrid();
        powerGrid.registerConsumer(globalPos, getPowerDemand(), factoryComponent::setPowered);
        powerGrid.setMaxConnections(globalPos, getMaxPowerConnections());
    }
}