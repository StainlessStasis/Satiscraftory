package io.github.stainlessstasis.manifold.item.power_link;

import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class PowerLinkTargets {
    private PowerLinkTargets() {}

    @Nullable
    public static BlockPos resolve(Level level, BlockPos clickedPos) {
        BlockPos mbControllerPos = MultiblockFillerRegistry.controllerPosAt(level, clickedPos);
        return mbControllerPos != null ? mbControllerPos : clickedPos;
    }
}