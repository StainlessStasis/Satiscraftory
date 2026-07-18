package io.github.stainlessstasis.manifold;

import java.util.PriorityQueue;

public class Scheduler {
    private final PriorityQueue<ScheduledEvent> events = new PriorityQueue<>();
    private long currentTick = 0;

    public ScheduledTask schedule(long tick, Runnable action) {
        ScheduledEvent event = new ScheduledEvent(tick, action);
        events.add(event);
        return event;
    }

    public void tick(long currentTick) {
        this.currentTick = currentTick;
        while (!events.isEmpty() && events.peek().tick <= currentTick) {
            ScheduledEvent event = events.poll();
            if (!event.cancelled) event.action.run();
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

    public interface ScheduledTask {
        void cancel();
    }

    private static final class ScheduledEvent implements Comparable<ScheduledEvent>, ScheduledTask {
        final long tick;
        final Runnable action;
        boolean cancelled = false;

        ScheduledEvent(long tick, Runnable action) {
            this.tick = tick;
            this.action = action;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public int compareTo(ScheduledEvent other) {
            return Long.compare(this.tick, other.tick);
        }
    }
}