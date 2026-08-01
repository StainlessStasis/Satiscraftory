package io.github.stainlessstasis.manifold.factory_power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class PowerProducerBlock extends BaseEntityBlock {
    public static final double DEFAULT_SUPPLY_RATE = 20d;

    public static final MapCodec<PowerProducerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("supply_rate").forGetter(PowerProducerBlock::getSupplyRate),
                    propertiesCodec()
            ).apply(instance, (supplyRate, properties) -> new PowerProducerBlock(properties, supplyRate))
    );

    private final double supplyRate;

    public PowerProducerBlock(Properties properties, double supplyRate) {
        super(properties);
        this.supplyRate = supplyRate;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public double getSupplyRate() {
        return supplyRate;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PowerProducerBlockEntity(pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).getPowerGrid().unregisterProducer(GlobalPos.of(level.dimension(), pos));
    }
}