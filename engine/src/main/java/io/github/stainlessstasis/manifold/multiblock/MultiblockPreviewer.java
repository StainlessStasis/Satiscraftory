package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface MultiblockPreviewer<B extends BaseEntityBlock & MultiblockPreviewer<B>> {
    @SuppressWarnings("unchecked")
    default B getPreviewBlock() {
        return (B) this;
    }

    MultiblockShape getMultiblockShape();
    BlockState getPreviewPlacement(BlockPlaceContext context);
}
