package com.example.examplemod.engine_internal.block.belt;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public enum BeltShape implements StringRepresentable {
    NORTH_SOUTH(Direction.SOUTH, 0, Direction.NORTH, 0, false),
    EAST_WEST(Direction.WEST, 0, Direction.EAST, 0, false),
    ASCENDING_NORTH(Direction.SOUTH, 0, Direction.NORTH, 1, false),
    ASCENDING_SOUTH(Direction.NORTH, 0, Direction.SOUTH, 1, false),
    ASCENDING_EAST(Direction.WEST, 0, Direction.EAST, 1, false),
    ASCENDING_WEST(Direction.EAST, 0, Direction.WEST, 1, false),
    NORTH_EAST(Direction.NORTH, 0, Direction.EAST, 0, true),
    NORTH_WEST(Direction.NORTH, 0, Direction.WEST, 0, true),
    SOUTH_EAST(Direction.SOUTH, 0, Direction.EAST, 0, true),
    SOUTH_WEST(Direction.SOUTH, 0, Direction.WEST, 0, true);

    private final Direction endADirection;
    private final int endAYOffset;
    private final Direction endBDirection;
    private final int endBYOffset;
    private final boolean corner;

    BeltShape(Direction endADirection, int endAYOffset, Direction endBDirection, int endBYOffset, boolean corner) {
        this.endADirection = endADirection;
        this.endAYOffset = endAYOffset;
        this.endBDirection = endBDirection;
        this.endBYOffset = endBYOffset;
        this.corner = corner;
    }

    public Direction endADirection() { return endADirection; }
    public int endAYOffset() { return endAYOffset; }
    public Direction endBDirection() { return endBDirection; }
    public int endBYOffset() { return endBYOffset; }
    public boolean isCorner() { return corner; }
    public boolean isAscending() { return endAYOffset != endBYOffset; }

    public Direction defaultInputDirection() { return endADirection; }
    public int defaultInputYOffset() { return endAYOffset; }
    public Direction defaultOutputDirection() { return endBDirection; }
    public int defaultOutputYOffset() { return endBYOffset; }

    public boolean connectsTo(Direction direction) {
        return endADirection == direction || endBDirection == direction;
    }

    @Override
    public @NonNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}