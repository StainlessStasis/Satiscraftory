package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.FactoryBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.power_producer.PowerProducerBlock;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class PowerableFactoryBlockEntity<T extends PowerableFactoryComponent> extends FactoryBlockEntity<T> {
    private PowerIndicatorState powerIndicatorState = PowerIndicatorState.NO_CONNECTION;

    public PowerableFactoryBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public PowerIndicatorState computePowerIndicatorState(ServerLevel level) {
        GlobalPos globalPos = GlobalPos.of(level.dimension(), getBlockPos());
        PowerGrid powerGrid = FactoryNetwork.get(level).getPowerGrid();

        if (powerGrid.getConnectionCount(globalPos) == 0) return PowerIndicatorState.NO_CONNECTION;
        if (!powerGrid.isPowered(globalPos)) return PowerIndicatorState.UNPOWERED;

        T factoryComponent = getFactoryComponent();
        boolean working = factoryComponent != null && factoryComponent.isActivelyWorking();
        return working ? PowerIndicatorState.WORKING : PowerIndicatorState.IDLE;
    }

    public void tickPowerIndicator(ServerLevel level) {
        PowerIndicatorState newState = computePowerIndicatorState(level);
        if (newState == powerIndicatorState) return;

        powerIndicatorState = newState;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), PowerProducerBlock.UPDATE_CLIENTS);
    }

    public PowerIndicatorState getPowerIndicatorState() {
        return powerIndicatorState;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putString("PowerIndicatorState", powerIndicatorState.name());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        String stateName = input.getStringOr("PowerIndicatorState", PowerIndicatorState.NO_CONNECTION.name());
        try {
            powerIndicatorState = PowerIndicatorState.valueOf(stateName);
        } catch (IllegalArgumentException _) {
            powerIndicatorState = PowerIndicatorState.NO_CONNECTION;
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
