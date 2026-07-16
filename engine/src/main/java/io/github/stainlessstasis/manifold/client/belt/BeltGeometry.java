package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BeltGeometry {
    public static final float HALF_WIDTH = 0.375f;
    public static final float SURFACE_HEIGHT = 0.625f;

    private static final float STRAIGHT_U0 = 2f,  STRAIGHT_U1 = 3.5f;
    private static final float STRAIGHT_V0 = 2.375f, STRAIGHT_V1 = 4.375f;
    // TODO:
    private static final float ASCENDING_U0 = 0f, ASCENDING_U1 = 0f;
    private static final float ASCENDING_V0 = 0f, ASCENDING_V1 = 0f;
    private static final float CORNER_U0 = 0f, CORNER_U1 = 0f;
    private static final float CORNER_V0 = 0f, CORNER_V1 = 0f;

    public static Vec3 localOffsetAt(BeltShape shape, boolean reversed, double t) {
        double u = reversed ? (1d - t) : t; // u=0 at endA, u=1 at endB, regardless of reversed or not
        Direction endA = shape.endADirection();
        Direction endB = shape.endBDirection();

        double ax = 0.5 + endA.getStepX() * 0.5, az = 0.5 + endA.getStepZ() * 0.5;
        double bx = 0.5 + endB.getStepX() * 0.5, bz = 0.5 + endB.getStepZ() * 0.5;
        double ay = shape.endAYOffset(), by = shape.endBYOffset();

        if (!shape.isCorner()) {
            double x = Mth.lerp(u, ax, bx);
            double z = Mth.lerp(u, az, bz);
            double y = Mth.lerp(u, ay, by);
            return new Vec3(x - 0.5, y, z - 0.5);
        }

        Direction dirNorthSouth = (endA.getAxis() == Direction.Axis.Z) ? endA : endB;
        Direction dirEastWest = (endA.getAxis() == Direction.Axis.X) ? endA : endB;
        double centerX = 0.5 + 0.5 * dirEastWest.getStepX();
        double centerZ = 0.5 + 0.5 * dirNorthSouth.getStepZ();

        double startAngle = Math.atan2(az - centerZ, ax - centerX);
        double endAngle = Math.atan2(bz - centerZ, bx - centerX);
        double delta = wrapRadians(endAngle - startAngle);
        double angle = startAngle + delta * u;

        double x = centerX + 0.5 * Math.cos(angle);
        double z = centerZ + 0.5 * Math.sin(angle);
        return new Vec3(x - 0.5, 0, z - 0.5);
    }

    public static float tiltDegrees(BeltShape shape, boolean reversed) {
        if (shape == BeltShape.ASCENDING_SOUTH || shape == BeltShape.ASCENDING_EAST) return -45f;
        return shape.isAscending() ? 45f : 0f;
    }

    public static float interpolatedTilt(
            BeltShape thisShape, boolean reversed, double position,
            @Nullable BeltShape neighborShapeAtStart, @Nullable BeltShape neighborShapeAtEnd
    ) {
        float thisTilt = tiltDegrees(thisShape, reversed);
        double blendWindow = 0.15;

        if (neighborShapeAtStart != null && position < blendWindow) {
            float neighborTilt = tiltDegrees(neighborShapeAtStart, reversed);
            float midTilt = (thisTilt + neighborTilt) / 2f;
            double blend = Mth.smoothstep(position / blendWindow);
            return (float) Mth.lerp(blend, midTilt, thisTilt);
        }

        if (neighborShapeAtEnd != null && position > 1 - blendWindow) {
            float neighborTilt = tiltDegrees(neighborShapeAtEnd, reversed);
            float midTilt = (thisTilt + neighborTilt) / 2f;
            double blend = Mth.smoothstep((1 - position) / blendWindow);
            return (float) Mth.lerp(blend, midTilt, thisTilt);
        }

        return thisTilt;
    }

    /**
     * True if the ascent runs along the Z axis (north/south)
     */
    public static boolean ascendsAlongZ(BeltShape shape) {
        return shape.endADirection().getAxis() == Direction.Axis.Z;
    }

    private static double wrapRadians(double angle) {
        return Math.IEEEremainder(angle, Mth.TWO_PI);
    }

    public record BeltStripQuad(Vec3 startLeft, Vec3 startRight, Vec3 endLeft, Vec3 endRight,
                                float u0, float u1, float v0, float v1) {
        Vec3 pointAt(float geomFraction, int side) {
            Vec3 start = (side == 0) ? startLeft : startRight;
            Vec3 end = (side == 0) ? endLeft : endRight;
            return new Vec3(
                    Mth.lerp(geomFraction, start.x, end.x),
                    Mth.lerp(geomFraction, start.y, end.y),
                    Mth.lerp(geomFraction, start.z, end.z)
            );
        }
    }

    public static BeltStripQuad stripQuadFor(BeltShape shape) {
        Direction endA = shape.endADirection();
        Direction endB = shape.endBDirection();

        if (shape.isCorner()) {
            return new BeltStripQuad(
                    new Vec3(0, SURFACE_HEIGHT, 0), new Vec3(1, SURFACE_HEIGHT, 0),
                    new Vec3(0, SURFACE_HEIGHT, 1), new Vec3(1, SURFACE_HEIGHT, 1),
                    0f, 1f, 0f, 1f
            );
        }

        double ax = 0.5 + endA.getStepX() * 0.5, az = 0.5 + endA.getStepZ() * 0.5;
        double bx = 0.5 + endB.getStepX() * 0.5, bz = 0.5 + endB.getStepZ() * 0.5;
        double ay = SURFACE_HEIGHT + shape.endAYOffset();
        double by = SURFACE_HEIGHT + shape.endBYOffset();

        double dirX = bx - ax, dirZ = bz - az;
        double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double perpX = -dirZ / length * HALF_WIDTH;
        double perpZ = dirX / length * HALF_WIDTH;

        Vec3 startLeft = new Vec3(ax - perpX, ay, az - perpZ);
        Vec3 startRight = new Vec3(ax + perpX, ay, az + perpZ);
        Vec3 endLeft = new Vec3(bx - perpX, by, bz - perpZ);
        Vec3 endRight = new Vec3(bx + perpX, by, bz + perpZ);

        return new BeltStripQuad(startLeft, startRight, endLeft, endRight, 0f, 1f, 0f, 1f);
    }
}