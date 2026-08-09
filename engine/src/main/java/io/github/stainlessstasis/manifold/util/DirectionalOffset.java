package io.github.stainlessstasis.manifold.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/**
 * Converts a local offset (forward/sideways/up, relative to a block's facing) into a world-space offset.
 */
public final class DirectionalOffset {
    private DirectionalOffset() {}

    public static Vec3 toWorld(Direction facing, double forward, double sideways, double up) {
        Direction side = facing.getClockWise();
        return new Vec3(
                facing.getStepX() * forward + side.getStepX() * sideways,
                up,
                facing.getStepZ() * forward + side.getStepZ() * sideways
        );
    }

    /**
     X = Sideways; Y = Up; Z = Forward
     */
    public static Vec3 toWorld(Direction facing, Vec3 offset) {
        return toWorld(facing, offset.z(), offset.x(), offset.y());
    }

    public static Direction facingOf(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
    }
}