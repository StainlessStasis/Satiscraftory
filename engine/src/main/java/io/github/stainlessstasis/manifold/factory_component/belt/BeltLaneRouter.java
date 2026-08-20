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
}