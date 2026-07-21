package io.github.stainlessstasis.satiscraftory.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

public class ResourceNodeFeature extends Feature<ResourceNodeConfig> {
    public static final int MAX_Y_DEVIATION = 4;
    public static final int MAX_SCAN_DEPTH = 12;

    public ResourceNodeFeature(Codec<ResourceNodeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResourceNodeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ResourceNodeConfig config = context.config();

        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, context.origin());
        if (!level.getFluidState(surfacePos).isEmpty()) {
            return false;
        }

        level.setBlock(surfacePos, config.markerState(), Block.UPDATE_ALL);

        int radius = config.radius().sample(random);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue; // reserved for the marker

                double jitter = 0.75 + random.nextDouble() * 0.5;
                double distSq = dx * dx + dz * dz;
                if (distSq > (radius * radius) * jitter) continue;

                BlockPos columnPos = surfacePos.offset(dx, 0, dz);
                BlockPos groundPos = findGroundPos(level, columnPos, surfacePos.getY());
                if (groundPos == null) continue;

                level.setBlock(groundPos, config.oreState(), Block.UPDATE_ALL);
                clearFoliageAbove(level, groundPos);
            }
        }

        return true;
    }

    @Nullable
    private static BlockPos findGroundPos(WorldGenLevel level, BlockPos columnPos, int referenceY) {
        int topY = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, columnPos).getY();

        for (int y = topY - 1; y >= topY - 1 - MAX_SCAN_DEPTH; y--) {
            BlockPos pos = new BlockPos(columnPos.getX(), y, columnPos.getZ());
            BlockState state = level.getBlockState(pos);

            if (!isReplaceable(state)) continue;

            if (Math.abs(y - referenceY) > MAX_Y_DEVIATION) {
                return null;
            }
            return pos;
        }
        return null;
    }

    private static boolean isReplaceable(BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && !state.is(BlockTags.LEAVES)
                && !state.is(BlockTags.LOGS);
    }

    private static void clearFoliageAbove(WorldGenLevel level, BlockPos groundPos) {
        BlockPos abovePos = groundPos.above();
        BlockState above = level.getBlockState(abovePos);
        if (above.is(BlockTags.REPLACEABLE)) {
            level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}