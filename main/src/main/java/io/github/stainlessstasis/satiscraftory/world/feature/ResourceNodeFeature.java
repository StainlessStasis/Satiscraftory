package io.github.stainlessstasis.satiscraftory.world.feature;

import com.mojang.serialization.Codec;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodePurity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ResourceNodeFeature extends Feature<ResourceNodeConfig> {
    public static final int MAX_Y_DEVIATION = 4;
    public static final int MAX_SCAN_DEPTH = 12;
    private static final int MAX_CLUSTER_PLACEMENT_ATTEMPTS = 8;
    private static final double MIN_NODE_SEPARATION = 12.0;
    private static final double MIN_NODE_SEPARATION_SQR = MIN_NODE_SEPARATION*MIN_NODE_SEPARATION;

    public ResourceNodeFeature(Codec<ResourceNodeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ResourceNodeConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ResourceNodeConfig config = context.config();
        BlockPos anchor = context.origin();

        return placeCluster(level, random, config, anchor);
    }

    public static boolean placeCluster(WorldGenLevel level, RandomSource random, ResourceNodeConfig config, BlockPos anchor) {
        int clusterSize = config.clusterSize().sample(random);
        List<BlockPos> placed = new ArrayList<>(clusterSize);

        for (int i = 0; i < clusterSize; i++) {
            BlockPos candidate = (i == 0) ? anchor : pickClusterOffset(anchor, random, placed, config);
            if (candidate == null) continue;

            if (placeSingleNode(level, random, config, candidate)) {
                placed.add(candidate);
            }
        }

        return !placed.isEmpty();
    }

    @Nullable
    private static BlockPos pickClusterOffset(BlockPos anchor, RandomSource random, List<BlockPos> existing, ResourceNodeConfig config) {
        for (int attempt = 0; attempt < MAX_CLUSTER_PLACEMENT_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            int dist = config.clusterSpread().sample(random);
            BlockPos candidate = anchor.offset(
                    (int) Math.round(Math.cos(angle) * dist),
                    0,
                    (int) Math.round(Math.sin(angle) * dist)
            );

            boolean tooClose = false;
            for (BlockPos other : existing) {
                if (other.distSqr(candidate) < MIN_NODE_SEPARATION_SQR) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) return candidate;
        }
        return null;
    }

    public static boolean placeSingleNode(WorldGenLevel level, RandomSource random, ResourceNodeConfig config, BlockPos origin) {
        BlockPos surfacePos = findGroundPos(level, origin, null);
        if (surfacePos == null) return false;

        BlockPos nodePos = surfacePos.below(2);
        level.setBlock(nodePos, config.nodeState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE) {
            nodeBE.setPurity(ResourceNodePurity.pickRandom(random));
        }

        int radius = config.radius().sample(random);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double jitter = 0.75 + random.nextDouble() * 0.5;
                double distSq = dx * dx + dz * dz;
                if (distSq > (radius * radius) * jitter) continue;

                BlockPos columnPos = surfacePos.offset(dx, 0, dz);
                BlockPos groundPos = findGroundPos(level, columnPos, surfacePos.getY());
                if (groundPos == null) continue;

                level.setBlock(groundPos, config.resourceState(), Block.UPDATE_ALL);
                clearFoliageAbove(level, groundPos);
            }
        }

        return true;
    }

    @Nullable
    private static BlockPos findGroundPos(WorldGenLevel level, BlockPos columnPos, @Nullable Integer referenceY) {
        BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnPos);
        BlockPos surfaceBlockPos = topPos.below();
        if (!level.getFluidState(surfaceBlockPos).isEmpty()) return null; // ocean / river surface

        for (int y = surfaceBlockPos.getY(); y >= surfaceBlockPos.getY() - MAX_SCAN_DEPTH; y--) {
            BlockPos pos = new BlockPos(columnPos.getX(), y, columnPos.getZ());
            BlockState state = level.getBlockState(pos);

            if (!isReplaceable(state)) continue;
            if (referenceY != null && Math.abs(y - referenceY) > MAX_Y_DEVIATION) return null;
            return pos;
        }
        return null;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.is(SCBlockTags.RESOURCE_NODE_REPLACEABLE);
    }

    private static void clearFoliageAbove(WorldGenLevel level, BlockPos groundPos) {
        BlockPos abovePos = groundPos.above();
        BlockState above = level.getBlockState(abovePos);
        if (above.is(BlockTags.REPLACEABLE)) {
            level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}