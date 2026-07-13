package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class BeltGeometry {
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
}