package io.github.stainlessstasis.manifold.factory_component.producer;


import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory_power.PowerableFactoryComponent;
import io.github.stainlessstasis.manifold.factory_component.FactoryComponent;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Producer implements PowerableFactoryComponent {
    public static final Identifier DEFAULT_ITEM_TYPE = ItemUtils.idOf(Items.RAW_IRON);
    public static final long DEFAULT_INTERVAL_TICKS = 1;

    private Identifier itemId;
    private long interval;
    private Port output;
    private final Scheduler scheduler;
    private Scheduler.@Nullable ScheduledTask productionTask;

    private int bufferedCount = 0;
    private boolean active = true;
    private long nextProductionTick;

    private boolean powered = true;
    private long pausedRemainingTicks = -1; // -1 = not paused

    private Producer(Identifier itemId, long interval, Port output, Scheduler scheduler, boolean active, int bufferedCount) {
        if (output == null) throw new IllegalArgumentException("Producer needs an output Port");
        this.itemId = itemId;
        this.interval = interval;
        this.output = output;
        this.scheduler = scheduler;
        this.active = active;
        this.bufferedCount = bufferedCount;
    }

    public Producer(Identifier itemId, long interval, Port output, Scheduler scheduler) {
        this(itemId, interval, output, scheduler, true, 0);
        scheduleNextProduction(scheduler.getCurrentTick() + interval);
    }

    public static Producer restore(
            Identifier itemId, long interval, Port output,
            Scheduler scheduler, boolean active, int bufferedCount, long nextProductionTick
    ) {
        Producer producer = new Producer(itemId, interval, output, scheduler, active, bufferedCount);
        if (!producer.isBufferFull()) {
            producer.scheduleNextProduction(nextProductionTick);
        }
        return producer;
    }

    public void setOutput(@NonNull Port output) {
        this.output = output;
    }

    @Override
    public void setOutputPort(int slot, Port port) {
        setOutput(port);
    }

    @Override
    public int outputSlotCount() {
        return 1;
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
        productionTask = null;
        if (!active || !powered) return;
        if (isBufferFull()) return;

        bufferedCount++;
        scheduleNextProduction(scheduler.getCurrentTick() + interval);
    }

    public void tick(long currentTick) {
        if (bufferedCount == 0) return;

        boolean wasFull = isBufferFull();
        Payload payload = new Payload(itemId, 1);
        if (!output.canAccept(payload)) return;

        output.accept(payload);
        bufferedCount--;

        if (wasFull && active && powered) {
            scheduleNextProduction(currentTick + interval);
        }
    }

    public void setActive(boolean active) {
        boolean wasActive = this.active;
        this.active = active;
        if (active && !wasActive && powered) {
            scheduleNextProduction(scheduler.getCurrentTick());
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setPowered(boolean powered) {
        if (this.powered == powered) return;
        this.powered = powered;

        if (!powered) {
            pauseForPowerLoss();
        } else {
            resumeFromPowerLoss();
        }
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public void pauseForPowerLoss() {
        if (productionTask == null) return;
        pausedRemainingTicks = nextProductionTick - scheduler.getCurrentTick();
        productionTask.cancel();
        productionTask = null;
    }

    @Override
    public void resumeFromPowerLoss() {
        if (pausedRemainingTicks < 0) return;
        long remainingTicks = pausedRemainingTicks;
        pausedRemainingTicks = -1;

        if (active && !isBufferFull()) {
            scheduleNextProduction(scheduler.getCurrentTick() + remainingTicks);
            if (productionTask == null) {
                scheduleNextProduction(scheduler.getCurrentTick() + interval);
            }
        }
    }

    @Override
    public boolean isActivelyWorking() {
        return productionTask != null;
    }

    /** True when the buffer holds a full stack of {@link #itemId} and can't accept another produced item. */
    public boolean isBufferFull() {
        return bufferedCount >= ItemUtils.maxStackSizeFor(itemId);
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

    /** How many of {@link #itemId} are currently buffered, waiting to leave via output. */
    public int getBufferedCount() {
        return bufferedCount;
    }

    /** Only meaningful when the buffer isn't full and the producer is powered; a full or unpowered producer has no standing scheduled event. */
    public long getNextProductionTick() {
        return nextProductionTick;
    }
}