package io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner;

import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlock;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.registry.block.MultiblockUnfilledSets;
import io.github.stainlessstasis.satiscraftory.registry.block.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BiomassBurnerBlock extends GeneratorBlock implements Multiblock<BiomassBurnerBlock> {
    public static final MultiblockShape MULTIBLOCK_SHAPE = new MultiblockShape(3, 3, 3, new BlockPos(1, 0, 0), MultiblockUnfilledSets.BIOMASS_BURNER);

    public BiomassBurnerBlock(Properties properties, Identifier generatorType, double powerRate) {
        super(properties, generatorType, powerRate);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new BiomassBurnerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return type == SCBlockEntities.BIOMASS_BURNER.get()
                ? (_, pos, state, be) -> BiomassBurnerBlockEntity.serverTick(serverLevel, pos, state, (BiomassBurnerBlockEntity) be)
                : null;
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

    @Override
    public MultiblockShape getMultiblockShape() {
        return MULTIBLOCK_SHAPE;
    }
}