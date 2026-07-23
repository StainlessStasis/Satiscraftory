package io.github.stainlessstasis.manifold.factory_component.producer;


import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Producer {
    public static final Identifier DEFAULT_ITEM_TYPE = ItemUtils.idOf(Items.RAW_IRON);
    public static final long DEFAULT_INTERVAL_TICKS = 1;

    private Identifier itemId;
    private long interval;
    private Port output;
    private final Scheduler scheduler;
    private Scheduler.@Nullable ScheduledTask productionTask;

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
        if (productionTask != null) productionTask.cancel();
        productionTask = scheduler.schedule(nextProductionTick, this::produce);
    }

    public void cancelScheduledTask() {
        if (productionTask != null) {
            productionTask.cancel();
            productionTask = null;
        }
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

    public void setItemId(Identifier itemId) {
        this.itemId = itemId;
    }
    public void setItemId(Item item) {
        this.itemId = ItemUtils.idOf(item);
    }
    public Identifier getItemId() {
        return itemId;
    }

    public void setInterval(long interval) {
        this.interval = interval;
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

