package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BeltGeometry {
    public static final float HALF_WIDTH = 0.375f;
    public static final float SURFACE_HEIGHT = 0.625f;
    public static final int CORNER_SEGMENTS = 32;
    public static final float CORNER_BULGE = 0.15f;

    // these values are derived from the up values of the belt cube in the model json,
    // but the belt cube is deleted from the json, since belts are rendered as quads with a scrolling texture
    private static final float STRAIGHT_U0 = 2f/16,  STRAIGHT_U1 = 3.5f/16;
    private static final float STRAIGHT_V0 = 2.375f/16, STRAIGHT_V1 = 4.375f/16;
    private static final float ASCENDING_U0 = 2.875f/16, ASCENDING_U1 = 4.25f/16;
    private static final float ASCENDING_V0 = 3.25f/16, ASCENDING_V1 = 6.125f/16;
    private static final float CORNER_U0 = 0f/16, CORNER_U1 = 3.5f/16;
    private static final float CORNER_V0 = 0f/16, CORNER_V1 = 4f/16;

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
        Vector3f pointAt(float geomFraction, int side) {
            Vec3 start = (side == 0) ? startLeft : startRight;
            Vec3 end = (side == 0) ? endLeft : endRight;
            return new Vector3f(
                    (float) Mth.lerp(geomFraction, start.x, end.x),
                    (float) Mth.lerp(geomFraction, start.y, end.y),
                    (float) Mth.lerp(geomFraction, start.z, end.z)
            );
        }
    }

    public static List<BeltStripQuad> stripQuadsFor(BeltShape shape) {
        if (!shape.isCorner()) return List.of(stripQuadFor(shape));
        return stripQuadsForCorner(shape, CORNER_SEGMENTS);
    }

    public static BeltStripQuad stripQuadFor(BeltShape shape) {
        Direction endA = shape.endADirection();
        Direction endB = shape.endBDirection();

        if (shape.isCorner()) {
            return new BeltStripQuad(
                    new Vec3(0, SURFACE_HEIGHT, 0), new Vec3(1, SURFACE_HEIGHT, 0),
                    new Vec3(0, SURFACE_HEIGHT, 1), new Vec3(1, SURFACE_HEIGHT, 1),
                    CORNER_U0, CORNER_U1, CORNER_V0, CORNER_V1
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

        float u0 = shape.isAscending() ? ASCENDING_U0 : STRAIGHT_U0;
        float u1 = shape.isAscending() ? ASCENDING_U1 : STRAIGHT_U1;
        float v0 = shape.isAscending() ? ASCENDING_V0 : STRAIGHT_V0;
        float v1 = shape.isAscending() ? ASCENDING_V1 : STRAIGHT_V1;

        return new BeltStripQuad(startLeft, startRight, endLeft, endRight, u0, u1, v0, v1);
    }

    private static List<BeltStripQuad> stripQuadsForCorner(BeltShape shape, int segments) {
        Direction endA = shape.endADirection();
        Direction endB = shape.endBDirection();
        Direction dirNorthSouth = (endA.getAxis() == Direction.Axis.Z) ? endA : endB;
        Direction dirEastWest = (endA.getAxis() == Direction.Axis.X) ? endA : endB;
        double centerX = 0.5 + 0.5 * dirEastWest.getStepX();
        double centerZ = 0.5 + 0.5 * dirNorthSouth.getStepZ();

        double ax = 0.5 + endA.getStepX() * 0.5, az = 0.5 + endA.getStepZ() * 0.5;
        double bx = 0.5 + endB.getStepX() * 0.5, bz = 0.5 + endB.getStepZ() * 0.5;
        double startAngle = Math.atan2(az - centerZ, ax - centerX);
        double endAngle = Math.atan2(bz - centerZ, bx - centerX);
        double delta = wrapRadians(endAngle - startAngle);
        boolean reverseWinding = delta > 0; // NORTH_WEST / SOUTH_EAST

        double innerRadius = 0.5 - HALF_WIDTH;

        List<BeltStripQuad> quads = new ArrayList<>();
        for (int i = 0; i < segments; i++) {
            double t0 = (double) i / segments, t1 = (double) (i + 1) / segments;
            double angle0 = startAngle + delta * t0, angle1 = startAngle + delta * t1;

            Vec3 left0 = arcPoint(centerX, centerZ, angle0, innerRadius);
            Vec3 right0 = arcPoint(centerX, centerZ, angle0, outerRadiusFor(t0));
            Vec3 left1 = arcPoint(centerX, centerZ, angle1, innerRadius);
            Vec3 right1 = arcPoint(centerX, centerZ, angle1, outerRadiusFor(t1));

            BeltStripQuad quad = reverseWinding
                    ? new BeltStripQuad(right0, left0, right1, left1, CORNER_U0, CORNER_U1, CORNER_V0, CORNER_V1)
                    : new BeltStripQuad(left0, right0, left1, right1, CORNER_U0, CORNER_U1, CORNER_V0, CORNER_V1);
            quads.add(quad);
        }
        return quads;
    }

    private static double outerRadiusFor(double t) {
        double bulge = Math.sin(t * Math.PI);
        return (0.5 + HALF_WIDTH) + bulge * CORNER_BULGE;
    }

    private static Vec3 arcPoint(double centerX, double centerZ, double angle, double radius) {
        return new Vec3(centerX + radius * Math.cos(angle), SURFACE_HEIGHT, centerZ + radius * Math.sin(angle));
    }
}