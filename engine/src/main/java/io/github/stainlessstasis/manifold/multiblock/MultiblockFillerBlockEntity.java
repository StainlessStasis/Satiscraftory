package io.github.stainlessstasis.manifold.multiblock;

import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MultiblockFillerBlockEntity extends BlockEntity {
    private @Nullable BlockPos controllerPos;

    public MultiblockFillerBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.MULTIBLOCK_FILLER.get(), pos, state);
    }

    public MultiblockFillerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        this.controllerPos = pos;
        setChanged();
    }

    public @Nullable BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        BlockPos capturedControllerPos = controllerPos;
        if (capturedControllerPos != null && level instanceof ServerLevel serverLevel && !MultiblockDemolition.isInProgress(serverLevel)) {
            MultiblockDemolition.demolishFromFiller(serverLevel, getBlockPos(), capturedControllerPos);
        }
    }


    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("ControllerPos", BlockPos.CODEC, controllerPos);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        controllerPos = input.read("ControllerPos", BlockPos.CODEC).orElse(null);
    }
}