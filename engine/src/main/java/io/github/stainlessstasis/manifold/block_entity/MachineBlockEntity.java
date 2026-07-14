package io.github.stainlessstasis.manifold.block_entity;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Machine;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class MachineBlockEntity extends BlockEntity {
    private static final Identifier DEFAULT_RECIPE_ID = Manifold.id("basic_processing");

    private Machine machine;

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.MACHINE.get(), pos, state);
    }

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());

        machine = network.getOrCreateMachine(globalPos, () -> {
            MachineRecipe recipe = ManifoldRecipes.get(DEFAULT_RECIPE_ID);
            if (recipe == null) {
                throw new IllegalStateException("Missing built-in recipe " + DEFAULT_RECIPE_ID + " - check Manifold's own datapack resources");
            }
            return new Machine(recipe, network.getScheduler(), List.of(FactoryNetwork.NO_OP_PORT));
        });

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        machine.assignOutputFace(facing, 0);
        machine.assignInputFace(facing.getOpposite(), 0);

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos selfPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        Direction outputDirection = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos outputPos = getBlockPos().relative(outputDirection);
        network.linkMachineOutput(selfPos, 0, GlobalPos.of(serverLevel.dimension(), outputPos), outputDirection);
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    private BlockPos resolveOutputPos() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getBlockPos().relative(facing);
    }

    public Machine getMachine() {
        return machine;
    }
}