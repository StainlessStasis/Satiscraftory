package io.github.stainlessstasis.satiscraftory.factory_component.power_pole;

import com.mojang.serialization.MapCodec;
import io.github.stainlessstasis.manifold.factory_component.AbstractDirectionalFactoryBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockPlacement;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.manifold.util.MessageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PowerPoleBlock extends AbstractDirectionalFactoryBlock implements Multiblock<PowerPoleBlock> {
    public static final MultiblockShape MULTIBLOCK_SHAPE = new MultiblockShape(1, 1, 4, new BlockPos(0, 0, 0));
    public static final MapCodec<PowerPoleBlock> CODEC = simpleCodec(PowerPoleBlock::new);

    public PowerPoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> type) {
        return null;
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {

    }

    @Override
    public MultiblockShape getMultiblockShape() {
        return MULTIBLOCK_SHAPE;
    }

    @Override
    public BlockState getPreviewPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new PowerPoleBlockEntity(SCBlockEntities.POWER_POLE.get(), pos, state);
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected float getShadeBrightness(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return 1f;
    }

    @Override
    protected boolean propagatesSkylightDown(@NonNull BlockState state) {
        return true;
    }
}
