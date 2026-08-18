package io.github.stainlessstasis.manifold.client.util;

public class ScreenUtils {
    public static boolean isInside(double x, double y, int boxX, int boxY, int boxWidth, int boxHeight) {
        return x >= boxX && x < boxX + boxWidth && y >= boxY && y < boxY + boxHeight;
    }
}
