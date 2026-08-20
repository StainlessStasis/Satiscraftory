package io.github.stainlessstasis.manifold.factory_component.belt;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the path a belt lane should follow between two endpoints.
 * Very dumb and only computes the most direct L-shaped route.
 * No obstacle avoidance or terrain awareness.
 */
public final class BeltLaneRouter {
    private BeltLaneRouter() {}

    public record LaneRoute(List<BlockPos> positions, boolean feasible) {
        public int length() {
            return positions.size();
        }
    }

    /**
     * @param start the position of the first belt (inclusive)
     * @param end the position of the last belt (inclusive)
     */
    public static LaneRoute route(BlockPos start, BlockPos end) {
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();

        if (dx == 0 && dy == 0 && dz == 0) {
            return new LaneRoute(List.of(start), true);
        }

        boolean primaryIsX = Math.abs(dx) >= Math.abs(dz);
        int primaryLength = primaryIsX ? Math.abs(dx) : Math.abs(dz);
        int secondaryLength = primaryIsX ? Math.abs(dz) : Math.abs(dx);

        Direction primaryDir = primaryIsX
                ? (dx >= 0 ? Direction.EAST : Direction.WEST)
                : (dz >= 0 ? Direction.SOUTH : Direction.NORTH);
        Direction secondaryDir = primaryIsX
                ? (dz >= 0 ? Direction.SOUTH : Direction.NORTH)
                : (dx >= 0 ? Direction.EAST : Direction.WEST);

        int climbSign = Integer.signum(dy);
        int totalClimb = Math.abs(dy);

        // Climbing and turning can't happen on the same block, so if there's any turn at all,
        // do all the climbing on the leg that ends the route (secondary leg) and keep the primary leg fully flat
        int climbInPrimary;
        int climbInSecondary;
        if (secondaryLength > 0) {
            climbInPrimary = 0;
            climbInSecondary = totalClimb;
            if (climbInSecondary > secondaryLength) {
                return new LaneRoute(List.of(), false);
            }
        } else {
            climbInPrimary = totalClimb;
            climbInSecondary = 0;
            if (climbInPrimary > primaryLength) {
                return new LaneRoute(List.of(), false);
            }
        }

        List<BlockPos> path = new ArrayList<>(primaryLength + secondaryLength + 1);
        BlockPos current = start;
        path.add(current);

        for (int i = 1; i <= primaryLength; i++) {
            current = i <= climbInPrimary
                    ? current.relative(primaryDir).above(climbSign)
                    : current.relative(primaryDir);
            path.add(current);
        }

        for (int i = 1; i <= secondaryLength; i++) {
            current = i <= climbInSecondary
                    ? current.relative(secondaryDir).above(climbSign)
                    : current.relative(secondaryDir);
            path.add(current);
        }

        return new LaneRoute(path, current.equals(end));
    }

    public record CellPlacement(BeltShape shape, boolean reversed) {}

    /**
     * Computes the shape and flow direction for the belt at path index {@code i}
     * @param laneReversed true for the "Lane Reversed" placement mode
     */
    public static CellPlacement deriveCellPlacement(List<BlockPos> path, int i, boolean laneReversed) {
        BlockPos current = path.get(i);
        BlockPos previous = i > 0 ? path.get(i - 1) : null;
        BlockPos next = i < path.size() - 1 ? path.get(i + 1) : null;

        Direction directionToPrevious = previous != null ? horizontalDirectionTo(current, previous) : null;
        Direction directionToNext = next != null ? horizontalDirectionTo(current, next) : null;

        boolean nextIsHigher = next != null && next.getY() == current.getY() + 1;
        boolean previousIsHigher = previous != null && previous.getY() == current.getY() + 1;

        BeltShape shape;
        if (nextIsHigher) {
            shape = ascendingTowards(directionToNext);
        } else if (previousIsHigher) {
            shape = ascendingTowards(directionToPrevious);
        } else if (directionToPrevious != null && directionToNext != null) {
            shape = (directionToPrevious.getAxis() == directionToNext.getAxis()) ? straightAlong(directionToPrevious) : cornerFor(directionToPrevious, directionToNext);
        } else {
            // only one neighbor, so this is one end of the lane
            Direction knownDirection = directionToPrevious != null ? directionToPrevious : directionToNext;
            shape = knownDirection != null ? straightAlong(knownDirection) : BeltShape.NORTH_SOUTH;
        }

        Direction flowDirection = next != null ? directionToNext : (previous != null ? directionToPrevious.getOpposite() : null);
        boolean reversed = flowDirection != null && shape.defaultOutputDirection() != flowDirection;
        if (laneReversed) reversed = !reversed;

        return new CellPlacement(shape, reversed);
    }

    private static BeltShape ascendingTowards(Direction uphillDirection) {
        return switch (uphillDirection) {
            case NORTH -> BeltShape.ASCENDING_NORTH;
            case SOUTH -> BeltShape.ASCENDING_SOUTH;
            case EAST -> BeltShape.ASCENDING_EAST;
            case WEST -> BeltShape.ASCENDING_WEST;
            default -> throw new IllegalArgumentException("Belts can't run vertically: " + uphillDirection);
        };
    }

    private static BeltShape straightAlong(Direction direction) {
        return direction.getAxis() == Direction.Axis.Z ? BeltShape.NORTH_SOUTH : BeltShape.EAST_WEST;
    }

    private static BeltShape cornerFor(Direction directionA, Direction directionB) {
        Direction northSouth = (directionA == Direction.NORTH || directionA == Direction.SOUTH) ? directionA : directionB;
        Direction eastWest = (directionA == Direction.EAST || directionA == Direction.WEST) ? directionA : directionB;
        return switch (northSouth) {
            case NORTH -> eastWest == Direction.EAST ? BeltShape.NORTH_EAST : BeltShape.NORTH_WEST;
            case SOUTH -> eastWest == Direction.EAST ? BeltShape.SOUTH_EAST : BeltShape.SOUTH_WEST;
            default -> throw new IllegalArgumentException("Not a flat corner pairing: " + directionA + ", " + directionB);
        };
    }

    private static Direction horizontalDirectionTo(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        if (dx == 1) return Direction.EAST;
        if (dx == -1) return Direction.WEST;
        if (dz == 1) return Direction.SOUTH;
        if (dz == -1) return Direction.NORTH;
        throw new IllegalArgumentException("Not a horizontal neighbor: " + from + " -> " + to);
    }
}