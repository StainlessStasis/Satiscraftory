package io.github.stainlessstasis.manifold.util;

public class BeltConstants {
    public static final int ITEMS_PER_BELT = 2;
    public static final float SCALE = 0.5f; // expected to be 1/ITEMS_PER_BELT - hardcoded because I don't want weird floating point rounding shit
    public static final double MIN_GAP = SCALE + 0.001;

    public static float getScaledBeltSpeed(float rawSpeed) {
        return rawSpeed/ITEMS_PER_BELT;
    }
}
