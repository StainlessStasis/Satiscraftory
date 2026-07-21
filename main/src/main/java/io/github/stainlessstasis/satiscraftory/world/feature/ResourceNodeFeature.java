package io.github.stainlessstasis.satiscraftory.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ResourceNodeFeature extends Feature<ResourceNodeConfig> {

    public ResourceNodeFeature(Codec<ResourceNodeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResourceNodeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ResourceNodeConfig config = context.config();

        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, context.origin());
        level.setBlock(surfacePos, config.markerState(), Block.UPDATE_ALL);

        int radius = config.radius().sample(random);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue; // reserved for the marker

                double jitter = 0.75 + random.nextDouble() * 0.5;
                double distSq = dx * dx + dz * dz;
                if (distSq > (radius * radius) * jitter) continue;

                BlockPos columnTop = level.getHeightmapPos(
                        Heightmap.Types.WORLD_SURFACE_WG, surfacePos.offset(dx, 0, dz)
                );
                BlockPos targetPos = columnTop.below();

                BlockState blockState = level.getBlockState(targetPos);
                if (!isReplaceable(blockState)) continue;

                level.setBlock(targetPos, config.oreState(), Block.UPDATE_ALL);
            }
        }

        return true;
    }

    private static boolean isReplaceable(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty();
    }
}