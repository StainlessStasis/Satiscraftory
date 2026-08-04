package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.util.DirectionalOffset;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface CableAnchorProvider {
    Vec3 getCableAnchorPos();

    /**
     * Gets the world-space offset of the cable from the block state and its local offset
     */
    default Vec3 getCableOffset(BlockState blockState, Vec3 localOffset) {
        Direction facing = DirectionalOffset.facingOf(blockState);
        Vec3 footprintCenter = new Vec3(0.5, 0, 0.5);
        Vec3 rotatedFromCenter = DirectionalOffset.toWorld(facing, localOffset.z(), localOffset.x(), 0);
        return footprintCenter.add(rotatedFromCenter).add(0, localOffset.y(), 0);
    }
}