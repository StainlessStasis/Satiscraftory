package io.github.stainlessstasis.manifold.factory_component.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.AbstractDirectionalFactoryBlock;
import io.github.stainlessstasis.manifold.item.power_link.PowerLinkItem;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class GeneratorBlock extends AbstractDirectionalFactoryBlock {
    private static final VoxelShape VISUAL_SHAPE = Shapes.box(-0.125, 0.0, -0.125, 1.125, 1.25, 1.125);
    private static final VoxelShape COLLISION_SHAPE = Shapes.block();

    public static final MapCodec<GeneratorBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("generator_type").forGetter(GeneratorBlock::getGeneratorType),
                    Codec.DOUBLE.fieldOf("power_rate").forGetter(GeneratorBlock::getPowerRate),
                    propertiesCodec()
            ).apply(instance, (generatorType, powerRate, properties) ->
                    new GeneratorBlock(properties, generatorType, powerRate))
    );

    private final Identifier generatorType;
    private final double powerRate;

    public GeneratorBlock(Properties properties, Identifier generatorType, double powerRate) {
        super(properties);
        this.generatorType = generatorType;
        this.powerRate = powerRate;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public Identifier getGeneratorType() {
        return generatorType;
    }

    public double getPowerRate() {
        return powerRate;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new GeneratorBlockEntity(ManifoldBlockEntities.GENERATOR.get(), pos, state);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof GeneratorBlockEntity generatorBE)) {
            return InteractionResult.PASS;
        }

        if (player.getMainHandItem().getItem() instanceof PowerLinkItem) {
            return InteractionResult.PASS;
        }

        player.openMenu(generatorBE);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {}

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork network = FactoryNetwork.get(level);
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        network.removeGenerator(globalPos);
        network.getPowerGrid().unregisterProducer(globalPos);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState blockState, @NonNull BlockEntityType<T> type) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return type == ManifoldBlockEntities.GENERATOR.get()
                ? (_, pos, state, be) -> GeneratorBlockEntity.serverTick(serverLevel, pos, state, (GeneratorBlockEntity) be)
                : null;
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