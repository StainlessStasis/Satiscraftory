package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public final class MultiblockDemolition {
    private static final Set<Level> IN_PROGRESS = Collections.newSetFromMap(new WeakHashMap<>());

    private MultiblockDemolition() {}

    public static boolean isInProgress(Level level) {
        return IN_PROGRESS.contains(level);
    }

    public static void demolishFillers(Level level, List<BlockPos> fillerPositions) {
        if (isInProgress(level)) return;
        IN_PROGRESS.add(level);
        try {
            for (BlockPos pos : fillerPositions) {
                if (level.getBlockState(pos).getBlock() instanceof MultiblockFillerBlock) {
                    level.removeBlock(pos, false);
                    MultiblockFillerRegistry.unregister(level, pos);
                }
            }
        } finally {
            IN_PROGRESS.remove(level);
        }
    }

    public static void demolishFromFiller(Level level, BlockPos fillerPos, BlockPos controllerPos) {
        if (isInProgress(level)) return;
        IN_PROGRESS.add(level);
        try {
            if (level.getBlockEntity(controllerPos) instanceof MultiblockControllerAccess controller) {
                for (BlockPos siblingPos : controller.getMultiblockFillerPositions()) {
                    if (siblingPos.equals(fillerPos)) continue;
                    if (level.getBlockState(siblingPos).getBlock() instanceof MultiblockFillerBlock) {
                        level.removeBlock(siblingPos, false);
                        MultiblockFillerRegistry.unregister(level, siblingPos);
                    }
                }
            }
            if (!level.getBlockState(controllerPos).isAir()) {
                level.removeBlock(controllerPos, false);
            }
        } finally {
            IN_PROGRESS.remove(level);
        }
    }
}