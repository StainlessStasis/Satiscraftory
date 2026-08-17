package io.github.stainlessstasis.satiscraftory.building.demolition;

import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerRegistry;
import io.github.stainlessstasis.satiscraftory.building.BuildingCatalog;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import io.github.stainlessstasis.satiscraftory.building.BuildingCosts;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves what the build gun's demolish action would do to a targeted block, without doing it
 */
public final class DemolitionResolver {
    private DemolitionResolver() {}

    public static @Nullable DemolitionTarget resolve(Level level, BlockPos targetPos) {
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

        List<BlockPos> allPositions = controllerPos == null
                ? List.of(targetPos)
                : gatherMultiblockPositions(level, controllerPos);

        return new DemolitionTarget(targetPos, canonicalPos, allPositions, canonicalItem, cost);
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