package io.github.stainlessstasis.manifold.block.belt;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class BeltShapes {
    private static final int STEPS = 4;
    private static final double THICKNESS = 10/16f;
    private static final Map<BeltShape, VoxelShape> SHAPES = new EnumMap<>(BeltShape.class);

    static {
        for (BeltShape shape : BeltShape.values()) {
            SHAPES.put(shape, shape.isAscending() ? ascending(shape) : Shapes.box(0, 0, 0, 1, THICKNESS, 1));
        }
    }

    private static VoxelShape ascending(BeltShape shape) {
        Direction.Axis axis = shape.endADirection().getAxis();

        double coordA = edgeCoordinate(shape.endADirection());
        double coordB = edgeCoordinate(shape.endBDirection());

        double heightA = THICKNESS + shape.endAYOffset();
        double heightB = THICKNESS + shape.endBYOffset();
        double top = Math.max(heightA, heightB);

        VoxelShape result = Shapes.empty();
        for (int i = 0; i < STEPS; i++) {
            double sliceMin = (double) i / STEPS;
            double sliceMax = (double) (i + 1) / STEPS;

            double surfaceAtMin = heightA + (heightB - heightA) * (sliceMin - coordA) / (coordB - coordA);
            double surfaceAtMax = heightA + (heightB - heightA) * (sliceMax - coordA) / (coordB - coordA);
            double surface = Math.max(surfaceAtMin, surfaceAtMax);
            double yMax = Math.min(top, surface);

            VoxelShape box = axis == Direction.Axis.Z
                    ? Shapes.box(0, 0, sliceMin, 1, yMax, sliceMax)
                    : Shapes.box(sliceMin, 0, 0, sliceMax, yMax, 1);

            result = Shapes.or(result, box);
        }
        return result.optimize();
    }

    /** 0 for the NORTH/WEST-facing edge of the block, 1 for the SOUTH/EAST-facing edge. */
    private static double edgeCoordinate(Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 0;
    }

    public static VoxelShape get(BeltShape shape) {
        return SHAPES.get(shape);
    }
}