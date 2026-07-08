package com.example.examplemod.core;


public class Producer {
    private final String itemType;
    private final long interval;
    private final Port output;
    private final Scheduler scheduler;

    private Payload pending = null;
    private boolean active = true;

    public Producer(String itemType, long interval, Port output, Scheduler scheduler) {
        if (output == null) throw new IllegalArgumentException("Producer needs an output Port");
        this.itemType = itemType;
        this.interval = interval;
        this.output = output;
        this.scheduler = scheduler;
        scheduleNextProduction(scheduler.getCurrentTick());
    }

    private void scheduleNextProduction(long fromTick) {
        scheduler.schedule(fromTick + interval, this::produce);
    }

    private void produce() {
        if (!active) return;
        if (pending != null) return;

        Payload payload = new Payload(itemType);
        if (output.canAccept(payload)) {
            output.accept(payload);
            scheduleNextProduction(scheduler.getCurrentTick());
        } else {
            pending = payload;
        }
    }

    public void tick(long currentTick) {
        if (pending != null && output.canAccept(pending)) {
            output.accept(pending);
            pending = null;
            scheduleNextProduction(currentTick);
        }
    }

    public void setActive(boolean active) {
        boolean wasActive = this.active;
        this.active = active;
        if (active && !wasActive) {
            scheduleNextProduction(scheduler.getCurrentTick());
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBlocked() {
        return pending != null;
    }
}
