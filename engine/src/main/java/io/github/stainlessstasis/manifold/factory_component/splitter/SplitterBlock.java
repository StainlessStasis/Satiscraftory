package io.github.stainlessstasis.manifold.factory_component.splitter;

import io.github.stainlessstasis.manifold.factory_component.AbstractDirectionalFactoryBlock;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class SplitterBlock extends AbstractDirectionalFactoryBlock {
    private static final VoxelShape VISUAL_SHAPE = Shapes.box(-0.125, 0.0, -0.125, 1.125, 1.25, 1.125);
    private static final VoxelShape COLLISION_SHAPE = Shapes.block();
    private static final MapCodec<SplitterBlock> CODEC = simpleCodec(SplitterBlock::new);

    public SplitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new SplitterBlockEntity(ManifoldBlockEntities.SPLITTER.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof SplitterBlockEntity splitterBE) splitterBE.onNeighborChanged();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeSplitter(GlobalPos.of(level.dimension(), pos));
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return VISUAL_SHAPE;
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return COLLISION_SHAPE;
    }
}