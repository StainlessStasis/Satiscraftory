package com.example.examplemod.engine;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Producer {
    private final String itemType;
    private final long interval;
    private Port output;
    private final Scheduler scheduler;

    private @Nullable Payload pending = null;
    private boolean active = true;
    private long nextProductionTick;

    private Producer(String itemType, long interval, Port output, Scheduler scheduler, boolean active, @Nullable Payload pending) {
        if (output == null) throw new IllegalArgumentException("Producer needs an output Port");
        this.itemType = itemType;
        this.interval = interval;
        this.output = output;
        this.scheduler = scheduler;
        this.active = active;
        this.pending = pending;
    }

    public Producer(String itemType, long interval, Port output, Scheduler scheduler) {
        this(itemType, interval, output, scheduler, true, null);
        schedule(scheduler.getCurrentTick() + interval);
    }

    public static Producer restore(
            String itemType, long interval, Port output, Scheduler scheduler, boolean active, Payload pending, long nextProductionTick) {
        Producer producer = new Producer(itemType, interval, output, scheduler, active, pending);
        if (pending == null) {
            producer.schedule(nextProductionTick);
        }
        return producer;
    }

    public void setOutput(@NonNull Port output) {
        this.output = output;
    }

    private void scheduleNextProduction(long fromTick) {
        schedule(fromTick + interval);
    }

    private void schedule(long atTick) {
        this.nextProductionTick = atTick;
        scheduler.schedule(atTick, this::produce);
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

    /**
     * Only needs to be called while blocked; no-op otherwise.
     */
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

    public String getItemType() {
        return itemType;
    }

    public long getInterval() {
        return interval;
    }

    /** The item currently stuck waiting for the output to accept it, or null if not jammed*/
    public @Nullable Payload getPending() {
        return pending;
    }

    /** Only meaningful when getPending() == null; a jammed producer has no standing scheduled event. */
    public long getNextProductionTick() {
        return nextProductionTick;
    }
}

