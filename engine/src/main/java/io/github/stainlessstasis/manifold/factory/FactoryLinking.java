package io.github.stainlessstasis.manifold.factory;

import io.github.stainlessstasis.manifold.block_entity.factory_component.BeltBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.factory_component.ContainerBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.factory_component.MachineBlockEntity;
import io.github.stainlessstasis.manifold.block_entity.factory_component.ProducerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FactoryLinking {
    public static void relinkSelfAndNeighbors(ServerLevel level, BlockPos pos) {
        relinkSelf(level, pos);
        relinkNeighbors(level, pos);
    }

    private static void relinkSelf(ServerLevel level, BlockPos pos) {
        FactoryNetwork network = FactoryNetwork.get(level);
        relinkIfFactoryComponent(level.getBlockEntity(pos), network);
    }

    public static void relinkNeighbors(ServerLevel level, BlockPos pos) {
        FactoryNetwork network = FactoryNetwork.get(level);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int yOffset = -1; yOffset <= 1; yOffset++) {
                BlockPos neighborPos = pos.relative(dir).above(yOffset);
                relinkIfFactoryComponent(level.getBlockEntity(neighborPos), network);
            }
        }
    }

    private static void relinkIfFactoryComponent(BlockEntity blockEntity, FactoryNetwork network) {
        if (blockEntity instanceof ProducerBlockEntity producerBE) {
            producerBE.relink(network);
        } else if (blockEntity instanceof BeltBlockEntity beltBE) {
            beltBE.relink(network);
        } else if (blockEntity instanceof MachineBlockEntity machineBE) {
            machineBE.relink(network);
        } else if (blockEntity instanceof ContainerBlockEntity containerBE) {
            containerBE.relink(network);
        }
    }
}