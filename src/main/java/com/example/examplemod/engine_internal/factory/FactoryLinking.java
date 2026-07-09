package com.example.examplemod.engine_internal.factory;


import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.example.examplemod.engine_internal.block_entity.ProducerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class FactoryLinking {
    private FactoryLinking() {}

    public static void relinkSelfAndNeighbors(ServerLevel level, BlockPos pos) {
        relinkSelf(level, pos);
        relinkNeighbors(level, pos);
    }

    private static void relinkSelf(ServerLevel level, BlockPos pos) {
        FactoryNetwork network = FactoryNetwork.get(level);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ProducerBlockEntity producerBE) {
            producerBE.relink(network);
        } else if (blockEntity instanceof BeltBlockEntity beltBE) {
            beltBE.relink(network);
        }
    }

    public static void relinkNeighbors(ServerLevel level, BlockPos pos) {
        FactoryNetwork network = FactoryNetwork.get(level);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof ProducerBlockEntity producerBE) {
                producerBE.relink(network);
            } else if (be instanceof BeltBlockEntity beltBE) {
                beltBE.relink(network);
            }
        }
    }
}
