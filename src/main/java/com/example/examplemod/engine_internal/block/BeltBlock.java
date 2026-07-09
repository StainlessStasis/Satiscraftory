package com.example.examplemod.engine_internal.block;

import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BeltBlock extends AbstractFactoryBlock {
    private static final MapCodec<BeltBlock> CODEC = simpleCodec(BeltBlock::new);

    public BeltBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BeltBlockEntity(InternalEngineBlockEntities.BELT.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof BeltBlockEntity beltBE) beltBE.onNeighborChanged();
    }
}
