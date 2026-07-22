package io.github.stainlessstasis.satiscraftory.block;

import io.github.stainlessstasis.manifold.block.factory_component.ProducerBlock;
import io.github.stainlessstasis.satiscraftory.registry.SCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class MinerBlock extends ProducerBlock {
    public static final int NODE_SEARCH_RADIUS = 5;

    public MinerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        return findNearbyResourceNode(level, pos) != null;
    }
    
    public static @Nullable BlockPos findNearbyResourceNode(LevelReader level, BlockPos pos) {
        BlockPos min = pos.offset(-NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS, -NODE_SEARCH_RADIUS);
        BlockPos max = pos.offset(NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS, NODE_SEARCH_RADIUS);

        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(candidate).is(SCBlocks.RESOURCE_NODE.get())) {
                return candidate.immutable();
            }
        }
        return null;
    }

}
