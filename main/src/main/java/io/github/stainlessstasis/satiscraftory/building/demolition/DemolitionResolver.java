package io.github.stainlessstasis.satiscraftory.building.demolition;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Laneable;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLane;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerRegistry;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.satiscraftory.building.BuildingCatalog;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import io.github.stainlessstasis.satiscraftory.building.BuildingCosts;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneCosts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves what the build gun's demolish action would do to a targeted block, without doing it
 */
public final class DemolitionResolver {
    private DemolitionResolver() {}

    public static @Nullable DemolitionTarget resolve(Level level, BlockPos targetPos, boolean groupAsLane) {
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.isAir()) return null;

        BlockPos controllerPos = controllerPosFor(level, targetPos, targetState);
        BlockPos canonicalPos = controllerPos != null ? controllerPos : targetPos;
        BlockState canonicalState = controllerPos != null ? level.getBlockState(canonicalPos) : targetState;
        if (canonicalState.isAir()) return null;

        Item canonicalItem = canonicalState.getBlock().asItem();
        if (canonicalItem == Items.AIR) return null;

        Identifier itemId = BuiltInRegistries.ITEM.getKey(canonicalItem);

        // build gun can only ever demolish factory buildings
        if (BuildingCatalog.byId(itemId).isEmpty()) return null;
        BuildingCost cost = level.isClientSide() ? BuildingCosts.getClientSide(itemId) : BuildingCosts.get(itemId);

        if (controllerPos != null) {
            List<BlockPos> allPositions = gatherMultiblockPositions(level, controllerPos);
            return new DemolitionTarget(targetPos, canonicalPos, allPositions, canonicalItem, cost, DemolitionTarget.TargetType.MULTIBLOCK);
        }

        if (groupAsLane && canonicalState.getBlock() instanceof Laneable) {
            List<BlockPos> laneBlocks = laneBlockPositionsFor(level, targetPos);
            if (laneBlocks != null && laneBlocks.size() > 1) {
                BlockPos head = laneBlocks.getFirst();
                return new DemolitionTarget(targetPos, head, laneBlocks, canonicalItem, cost, DemolitionTarget.TargetType.LANE);
            }
        }

        return new DemolitionTarget(targetPos, targetPos, List.of(targetPos), canonicalItem, cost, DemolitionTarget.TargetType.SINGLE);
    }

    private static @Nullable List<BlockPos> laneBlockPositionsFor(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), pos);
            BeltLane lane = FactoryNetwork.get(serverLevel).getLaneManager().laneAt(globalPos);
            if (lane == null) return null;
            List<BlockPos> blocks = lane.getBlocks().stream().map(GlobalPos::pos).toList();
            return blocks.isEmpty() ? null : blocks;
        }

        if (level.getBlockEntity(pos) instanceof BeltBlockEntity beltEntity) {
            List<BlockPos> laneBlocks = beltEntity.getSyncedLaneBlocks();
            return laneBlocks.isEmpty() ? null : laneBlocks;
        }

        return null;
    }

    public static List<RecipeIngredient> computeRefund(Level level, DemolitionTarget target) {
        if (target.cost() == null) return List.of();

        if (target.targetType() == DemolitionTarget.TargetType.MULTIBLOCK) {
            return target.cost().inputs();
        }

        if (target.targetType() == DemolitionTarget.TargetType.LANE) {
            return sumRefundForBlocks(level, target.allPositions(), target.cost());
        }

        return refundForSingleBlock(level, target.primaryPos(), target.cost());
    }

    private static List<RecipeIngredient> sumRefundForBlocks(Level level, List<BlockPos> positions, BuildingCost cost) {
        Map<Identifier, Integer> totals = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            for (RecipeIngredient ingredient : refundForSingleBlock(level, pos, cost)) {
                totals.merge(ingredient.itemId(), ingredient.amount(), Integer::sum);
            }
        }

        List<RecipeIngredient> result = new ArrayList<>(totals.size());
        totals.forEach((itemId, amount) -> result.add(new RecipeIngredient(itemId, amount)));
        return result;
    }

    private static List<RecipeIngredient> refundForSingleBlock(Level level, BlockPos pos, BuildingCost cost) {
        if (level.getBlockEntity(pos) instanceof BeltBlockEntity beltEntity) {
            if (beltEntity.hasRefundShare()) return beltEntity.getRefundShare();
            return LaneCosts.perBlockFallbackCost(cost);
        }

        return cost.inputs();
    }

    private static @Nullable BlockPos controllerPosFor(Level level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof MultiblockFillerBlock) {
            return MultiblockFillerRegistry.controllerPosAt(level, pos);
        }
        if (level.getBlockEntity(pos) instanceof MultiblockControllerAccess) {
            return pos;
        }
        return null;
    }

    private static List<BlockPos> gatherMultiblockPositions(Level level, BlockPos controllerPos) {
        if (!(level.getBlockEntity(controllerPos) instanceof MultiblockControllerAccess controller)) {
            return List.of(controllerPos);
        }
        List<BlockPos> positions = new ArrayList<>(controller.getMultiblockFillerPositions());
        positions.add(controllerPos);
        return positions;
    }
}