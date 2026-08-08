package io.github.stainlessstasis.manifold.factory_component.generator;

import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.PowerProducingFactoryBlockEntity;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GeneratorBlockEntity extends PowerProducingFactoryBlockEntity<Generator> {
    private Generator generator;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        this(ManifoldBlockEntities.GENERATOR.get(), pos, state);
    }

    public GeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());

        generator = network.getOrCreateGenerator(globalPos, () -> {
            if (!(getBlockState().getBlock() instanceof GeneratorBlock generatorBlock)) {
                throw new IllegalStateException("GeneratorBlockEntity placed on a non-GeneratorBlock");
            }
            return new Generator(
                    generatorBlock.getGeneratorType(),
                    generatorBlock.getPowerRate(),
                    network.getScheduler()
            );
        });
        generator.setInputDirection(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));

        registerPowerProducer(serverLevel);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, GeneratorBlockEntity generatorBE) {
        generatorBE.updatePowerSupply(level);
        generatorBE.tickPowerIndicator(level);
    }

    @Override
    public Generator getFactoryComponent() {
        return generator;
    }
}
