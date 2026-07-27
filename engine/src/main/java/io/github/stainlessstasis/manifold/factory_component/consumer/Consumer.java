package io.github.stainlessstasis.manifold.factory_component.consumer;

import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class Consumer implements Port {
    private final int capacity;
    private final int processTime;
    private Direction inputDirection;
    private final Deque<Payload> buffer = new ArrayDeque<>();
    private @Nullable Payload processing = null;
    private long processStartTick = -1;
    private int consumedCount = 0;

    public Consumer(int capacity, int processTime) {
        this.capacity = capacity;
        this.processTime = processTime;
    }

    @Override
    public boolean canAccept(Payload payload) {
        return buffer.size() < capacity;
    }

    @Override
    public void accept(Payload payload) {
        if (!canAccept(payload)) throw new IllegalStateException("Consumer buffer overflow");
        buffer.add(payload);
    }

    public void tick(long currentTick) {
        if (processing == null && !buffer.isEmpty()) {
            processing = buffer.poll();
            processStartTick = currentTick;
        }
        if (processing != null && currentTick - processStartTick >= processTime) {
            consumedCount++;
            processing = null;
        }
    }

    public int getConsumedCount() {
        return consumedCount;
    }

    public int getBufferedCount() {
        return buffer.size();
    }

    public boolean isProcessing() {
        return processing != null;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getProcessTime() {
        return processTime;
    }

    public void setInputDirection(Direction direction) {
        this.inputDirection = direction;
    }

    public boolean acceptsFrom(Direction direction) {
        return inputDirection == null || direction == inputDirection;
    }

    public List<Identifier> getBufferedItemIds() {
        List<Identifier> ids = new ArrayList<>();
        for (Payload payload : buffer) ids.add(payload.itemId());
        return ids;
    }

    /** Identifier of the item currently mid-process, or null if nothing is processing. */
    public @Nullable Identifier getProcessingItemId() {
        return processing != null ? processing.itemId() : null;
    }

    public long getProcessStartTick() {
        return processStartTick;
    }

    public static Consumer restore(
            int capacity, int processTime, List<Identifier> bufferedItemIds, Identifier processingItemId, long processStartTick, int consumedCount)
    {
        Consumer consumer = new Consumer(capacity, processTime);
        for (Identifier itemId : bufferedItemIds) consumer.buffer.add(new Payload(itemId));
        if (processingItemId != null) {
            consumer.processing = new Payload(processingItemId);
            consumer.processStartTick = processStartTick;
        }
        consumer.consumedCount = consumedCount;
        return consumer;
    }
}

