package com.example.examplemod.block;

import com.example.examplemod.engine.FactoryLinking;
import com.example.examplemod.engine.FactoryNetwork;
import com.example.examplemod.engine.Machine;
import com.example.examplemod.engine.Recipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        machine = network.getOrCreateMachine(getBlockPos(),
                () -> new Machine(RECIPE, network.getScheduler(), FactoryNetwork.NO_OP_PORT));

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        network.linkMachineOutput(getBlockPos(), resolveOutputPos());
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

    public static void onBlockBroken(ServerLevel level, BlockPos pos) {
        FactoryNetwork.get(level).removeMachine(pos);
    }
}
