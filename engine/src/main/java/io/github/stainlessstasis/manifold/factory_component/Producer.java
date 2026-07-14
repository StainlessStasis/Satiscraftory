package io.github.stainlessstasis.manifold.factory_component;


import io.github.stainlessstasis.manifold.Scheduler;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Producer {
    private final Identifier itemId;
    private final long interval;
    private Port output;
    private final Scheduler scheduler;

    private @Nullable Payload pending = null;
    private boolean active = true;
    private long nextProductionTick;

    private Producer(Identifier itemId, long interval, Port output, Scheduler scheduler, boolean active, @Nullable Payload pending) {
        if (output == null) throw new IllegalArgumentException("Producer needs an output Port");
        this.itemId = itemId;
        this.interval = interval;
        this.output = output;
        this.scheduler = scheduler;
        this.active = active;
        this.pending = pending;
    }

    public Producer(Identifier itemId, long interval, Port output, Scheduler scheduler) {
        this(itemId, interval, output, scheduler, true, null);
        scheduleNextProduction(scheduler.getCurrentTick() + interval);
    }

    public static Producer restore(
            Identifier itemId, long interval, Port output, Scheduler scheduler, boolean active, Payload pending, long nextProductionTick) {
        Producer producer = new Producer(itemId, interval, output, scheduler, active, pending);
        if (pending == null) {
            producer.scheduleNextProduction(nextProductionTick);
        }
        return producer;
    }

    public void setOutput(@NonNull Port output) {
        this.output = output;
    }

    private void scheduleNextProduction(long nextProductionTick) {
        this.nextProductionTick = nextProductionTick;
        scheduler.schedule(nextProductionTick, this::produce);
    }

    private void produce() {
        if (!active) return;
        if (pending != null) return;

        Payload payload = new Payload(itemId);
        if (output.canAccept(payload)) {
            output.accept(payload);
            scheduleNextProduction(scheduler.getCurrentTick() + interval);
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
            scheduleNextProduction(currentTick + interval);
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

    public Identifier getItemId() {
        return itemId;
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

