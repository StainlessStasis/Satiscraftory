package com.example.examplemod.engine_internal.block_entity;

import com.example.examplemod.engine_internal.factory.FactoryLinking;
import com.example.examplemod.engine_internal.factory.FactoryNetwork;
import com.example.examplemod.engine_internal.Machine;
import com.example.examplemod.engine_internal.Recipe;
import com.example.examplemod.engine_internal.registry.InternalEngineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MachineBlockEntity extends BlockEntity {
    private static final Recipe RECIPE = new Recipe(
            Items.RAW_IRON.toString(), Items.IRON_INGOT.toString(), 60L
    );

    private Machine machine;

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(InternalEngineBlockEntities.MACHINE.get(), pos, state);
    }

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        machine = network.getOrCreateMachine(GlobalPos.of(serverLevel.dimension(), getBlockPos()),
                () -> new Machine(RECIPE, network.getScheduler(), FactoryNetwork.NO_OP_PORT));

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        network.linkMachineOutput(
                GlobalPos.of(serverLevel.dimension(), getBlockPos()),
                GlobalPos.of(serverLevel.dimension(), resolveOutputPos())
        );
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
