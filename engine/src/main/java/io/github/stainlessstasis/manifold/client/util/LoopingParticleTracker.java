package io.github.stainlessstasis.manifold.client.util;

import java.util.Map;
import java.util.WeakHashMap;

public class LoopingParticleTracker<T> {
    private final Map<T, Long> lastEmitTimeMs = new WeakHashMap<>();

    public void emitIfDue(T owner, boolean active, long currentTimeMs, long intervalMs, Runnable spawner) {
        if (!active) {
            lastEmitTimeMs.remove(owner);
            return;
        }
        Long last = lastEmitTimeMs.get(owner);
        if (last != null && currentTimeMs - last < intervalMs) return;
        lastEmitTimeMs.put(owner, currentTimeMs);
        spawner.run();
    }
}