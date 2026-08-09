package io.github.stainlessstasis.manifold.util;

/**
 * Debounces a boolean so that it only flips after staying at the new value for a number of consecutive calls
 */
public final class TickDebouncer {
    private final int riseThresholdTicks;
    private final int fallThresholdTicks;
    private boolean debouncedValue;
    private int consecutiveTicks;

    public TickDebouncer(boolean initialValue, int thresholdTicks) {
        this(initialValue, thresholdTicks, thresholdTicks);
    }

    public TickDebouncer(boolean initialValue, int riseThresholdTicks, int fallThresholdTicks) {
        if (riseThresholdTicks < 1 || fallThresholdTicks < 1) {
            throw new IllegalArgumentException("Thresholds must be >= 1");
        }
        this.riseThresholdTicks = riseThresholdTicks;
        this.fallThresholdTicks = fallThresholdTicks;
        this.debouncedValue = initialValue;
        this.consecutiveTicks = 0;
    }

    public boolean update(boolean rawValue) {
        if (rawValue == debouncedValue) {
            consecutiveTicks = 0;
            return false;
        }

        consecutiveTicks++;
        int threshold = rawValue ? riseThresholdTicks : fallThresholdTicks;
        if (consecutiveTicks < threshold) {
            return false;
        }

        debouncedValue = rawValue;
        consecutiveTicks = 0;
        return true;
    }

    public boolean get() {
        return debouncedValue;
    }

    public void restore(boolean value) {
        this.debouncedValue = value;
        this.consecutiveTicks = 0;
    }
}