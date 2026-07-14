package io.github.stainlessstasis.manifold.block;

import io.github.stainlessstasis.manifold.block_entity.ConsumerBlockEntity;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ConsumerBlock extends AbstractDirectionalFactoryBlock {
    private static final MapCodec<ConsumerBlock> CODEC = simpleCodec(ConsumerBlock::new);

    public ConsumerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ConsumerBlockEntity(ManifoldBlockEntities.CONSUMER.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {}

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeConsumer(GlobalPos.of(level.dimension(), pos));
    }
}
