package com.example.examplemod.engine;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class Consumer implements Port {
    private final int capacity;
    private final int processTime;

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

    public List<String> getBufferedTypeIds() {
        List<String> ids = new ArrayList<>();
        for (Payload payload : buffer) ids.add(payload.typeId());
        return ids;
    }

    /** Type id of the item currently mid-process, or null if nothing is processing. */
    public @Nullable String getProcessingTypeId() {
        return processing != null ? processing.typeId() : null;
    }

    public long getProcessStartTick() {
        return processStartTick;
    }

    public static Consumer restore(
            int capacity, int processTime, List<String> bufferedTypeIds, String processingTypeId, long processStartTick, int consumedCount)
    {
        Consumer consumer = new Consumer(capacity, processTime);
        for (String typeId : bufferedTypeIds) consumer.buffer.add(new Payload(typeId));
        if (processingTypeId != null) {
            consumer.processing = new Payload(processingTypeId);
            consumer.processStartTick = processStartTick;
        }
        consumer.consumedCount = consumedCount;
        return consumer;
    }
}

