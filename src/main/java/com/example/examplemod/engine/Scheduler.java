package com.example.examplemod.engine;

import java.util.PriorityQueue;

public class Scheduler {
    private final PriorityQueue<ScheduledEvent> events = new PriorityQueue<>();
    private long currentTick = 0;

    public void schedule(long tick, Runnable action) {
        events.add(new ScheduledEvent(tick, action));
    }

    public void tick(long currentTick) {
        this.currentTick = currentTick;
        while (!events.isEmpty() && events.peek().tick <= currentTick) {
            events.poll().action.run();
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int pendingCount() {
        return events.size();
    }

    private record ScheduledEvent(long tick, Runnable action) implements Comparable<ScheduledEvent> {
        @Override
        public int compareTo(ScheduledEvent other) {
            return Long.compare(this.tick, other.tick);
        }
    }
}
