package io.github.stainlessstasis.manifold.block;

import io.github.stainlessstasis.manifold.block_entity.ProducerBlockEntity;
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

public class ProducerBlock extends AbstractDirectionalFactoryBlock {
    private static final MapCodec<ProducerBlock> CODEC = simpleCodec(ProducerBlock::new);

    public ProducerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ProducerBlockEntity(ManifoldBlockEntities.PRODUCER.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof ProducerBlockEntity producerBE) producerBE.onNeighborChanged();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeProducer(GlobalPos.of(level.dimension(), pos));
    }
}
