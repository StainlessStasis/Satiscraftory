package io.github.stainlessstasis.manifold.block_entity;

import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Merger;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MergerBlockEntity extends BlockEntity {
    private Merger merger;

    public MergerBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.MERGER.get(), pos, state);
    }

    public MergerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        merger = network.getOrCreateMerger(globalPos, Merger::new);

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        merger.assignInputFace(facing.getOpposite(), 0);
        merger.assignInputFace(facing.getClockWise(), 1);
        merger.assignInputFace(facing.getCounterClockWise(), 2);

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos selfPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        Direction outputDirection = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos outputPos = getBlockPos().relative(outputDirection);
        network.linkMergerOutput(selfPos, GlobalPos.of(serverLevel.dimension(), outputPos), outputDirection);
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    public Merger getMerger() {
        return merger;
    }
}