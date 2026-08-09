package io.github.stainlessstasis.manifold.factory_component.generator;

import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.PowerProducingFactoryBlockEntity;
import io.github.stainlessstasis.manifold.menu.generator.GeneratorContainerData;
import io.github.stainlessstasis.manifold.menu.generator.GeneratorMenu;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.MenuConstants.PLAYER_INV_X;
import static io.github.stainlessstasis.manifold.menu.MenuConstants.PLAYER_INV_Y;

public class GeneratorBlockEntity extends PowerProducingFactoryBlockEntity<Generator> implements MenuProvider, IMenuProviderExtension {
    private static final int SLOT_X = PLAYER_INV_X;
    private static final int SLOT_Y = PLAYER_INV_Y/2 - 10;

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
    public void writeClientSideData(@NonNull AbstractContainerMenu menu, @NonNull RegistryFriendlyByteBuf buf) {
        buf.writeIdentifier(generator.getGeneratorType());
        buf.writeDouble(generator.getPowerRate());
        buf.writeVarInt(SLOT_X);
        buf.writeVarInt(SLOT_Y);
        buf.writeVarInt(PLAYER_INV_X);
        buf.writeVarInt(PLAYER_INV_Y);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory playerInventory, @NonNull Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return new GeneratorMenu(
                containerId, playerInventory, generator, generator.getPowerRate(),
                SLOT_X, SLOT_Y, PLAYER_INV_X, PLAYER_INV_Y,
                ContainerLevelAccess.create(serverLevel, getBlockPos()),
                new GeneratorContainerData(generator, serverLevel::getGameTime)
        );
    }

    @Override
    public @NonNull Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public Generator getFactoryComponent() {
        return generator;
    }
}