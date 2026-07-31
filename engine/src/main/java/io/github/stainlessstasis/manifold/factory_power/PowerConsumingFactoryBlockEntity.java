package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.Config;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.FactoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class PowerConsumingFactoryBlockEntity<T extends PowerableFactoryComponent> extends FactoryBlockEntity<T> {
    public PowerConsumingFactoryBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public abstract double getPowerDemand();

    public void registerPowerConsumer(ServerLevel level) {
        if (Config.POWER_REQUIRED.isFalse()) return;

        T factoryComponent = getFactoryComponent();
        if (factoryComponent == null) return;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), getBlockPos());
        PowerGrid powerGrid = FactoryNetwork.get(level).getPowerGrid();
        powerGrid.registerConsumer(globalPos, getPowerDemand(), factoryComponent::setPowered);
    }

    public void unregisterPowerConsumer(ServerLevel serverLevel) {
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        FactoryNetwork.get(serverLevel).getPowerGrid().unregisterConsumer(globalPos);
    }
}
