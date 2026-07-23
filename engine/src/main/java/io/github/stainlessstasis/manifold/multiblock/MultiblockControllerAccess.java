package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import java.util.List;

public interface MultiblockControllerAccess {
    List<BlockPos> getMultiblockFillerPositions();
}