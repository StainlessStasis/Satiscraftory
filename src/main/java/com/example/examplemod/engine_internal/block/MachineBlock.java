package com.example.examplemod.engine_internal.block;

import com.example.examplemod.engine_internal.block_entity.MachineBlockEntity;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MachineBlock extends AbstractDirectionalFactoryBlock {
    private static final MapCodec<MachineBlock> CODEC = simpleCodec(MachineBlock::new);

    public MachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new MachineBlockEntity(InternalEngineBlockEntities.MACHINE.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof MachineBlockEntity machineBE) machineBE.onNeighborChanged();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeMachine(GlobalPos.of(level.dimension(), pos));
    }
}
