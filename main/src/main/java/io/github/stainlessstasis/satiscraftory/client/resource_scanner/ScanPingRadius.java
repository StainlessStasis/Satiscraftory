package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

/**
 * Used for the scan effect shader
 */
public final class ScanPingRadius {
    private ScanPingRadius() {}
    private static final float INITIAL_RADIUS = 12f;
    private static final float TIME_OFFSET_MILLIS = 200f;

    public static float computeRadius(long startMillis, float durationMillis, float targetRadius) {
        float timeOffset = TIME_OFFSET_MILLIS;
        float normalizationFactor = 1f / ((durationMillis + timeOffset) * (durationMillis + timeOffset) - timeOffset * timeOffset);
        float radiusOffset = -targetRadius * timeOffset * timeOffset * normalizationFactor;
        float radiusScale = targetRadius * normalizationFactor;
        float elapsedTime = System.currentTimeMillis() - startMillis;
        return INITIAL_RADIUS + radiusOffset + (elapsedTime + timeOffset) * (elapsedTime + timeOffset) * radiusScale;
    }

    public static boolean isActive(long startMillis, float durationMillis) {
        return startMillis > 0 && (System.currentTimeMillis() - startMillis) < durationMillis;
    }
}