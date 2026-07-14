package io.github.stainlessstasis.manifold.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class FactoryUtils {
    public static Direction getOutputDirection(BlockPos pos, BlockPos outputPos) {
        return Direction.getNearest(
                outputPos.getX() - pos.getX(),
                0,
                outputPos.getZ() - pos.getZ(),
                Direction.NORTH // fallback
        );
    }
}
