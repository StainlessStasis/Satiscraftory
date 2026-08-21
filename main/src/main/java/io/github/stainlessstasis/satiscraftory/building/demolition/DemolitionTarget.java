package io.github.stainlessstasis.satiscraftory.building.demolition;

import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record DemolitionTarget(
        BlockPos primaryPos, BlockPos canonicalPos, List<BlockPos> allPositions,
        Item canonicalItem, @Nullable BuildingCost cost, TargetType targetType
) {
    public enum TargetType {
        SINGLE, MULTIBLOCK, LANE
    }

    public boolean isMultiblock() {
        return targetType == TargetType.MULTIBLOCK;
    }

    public boolean isLane() {
        return targetType == TargetType.LANE;
    }
}