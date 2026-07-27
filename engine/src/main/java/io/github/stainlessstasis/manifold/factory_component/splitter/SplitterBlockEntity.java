package io.github.stainlessstasis.manifold.factory_component.splitter;

import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SplitterBlockEntity extends BlockEntity {
    private Splitter splitter;

    public SplitterBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.SPLITTER.get(), pos, state);
    }

    public SplitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        splitter = network.getOrCreateSplitter(globalPos, Splitter::new);

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        splitter.assignOutputFace(facing, 0);
        splitter.assignOutputFace(facing.getClockWise(), 1);
        splitter.assignOutputFace(facing.getCounterClockWise(), 2);

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos selfPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction[] outputDirections = { facing, facing.getClockWise(), facing.getCounterClockWise() };

        for (int slot = 0; slot < outputDirections.length; slot++) {
            Direction dir = outputDirections[slot];
            BlockPos outPos = getBlockPos().relative(dir);
            network.linkSplitterOutput(selfPos, slot, GlobalPos.of(serverLevel.dimension(), outPos), dir);
        }
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    public Splitter getSplitter() {
        return splitter;
    }
}