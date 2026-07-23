package io.github.stainlessstasis.manifold.block;

import com.mojang.serialization.MapCodec;
import io.github.stainlessstasis.manifold.block_entity.MultiblockFillerBlockEntity;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MultiblockFillerBlock extends BaseEntityBlock {
    public static final MapCodec<MultiblockFillerBlock> CODEC = simpleCodec(MultiblockFillerBlock::new);

    public MultiblockFillerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new MultiblockFillerBlockEntity(ManifoldBlockEntities.MULTIBLOCK_FILLER.get(), pos, state);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos,
            @NonNull Player player, @NonNull BlockHitResult hitResult
    ) {
        BlockPos controllerPos = controllerPosAt(level, pos);
        if (controllerPos == null) return InteractionResult.PASS;

        BlockState controllerState = level.getBlockState(controllerPos);
        BlockHitResult redirected = new BlockHitResult(
                hitResult.getLocation(), hitResult.getDirection(), controllerPos, hitResult.isInside()
        );
        return controllerState.useWithoutItem(level, player, redirected);
    }

    private @Nullable BlockPos controllerPosAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiblockFillerBlockEntity fillerBE
                ? fillerBE.getControllerPos()
                : null;
    }
}