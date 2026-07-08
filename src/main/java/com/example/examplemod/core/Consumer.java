package com.example.examplemod.core;

import java.util.ArrayDeque;
import java.util.Deque;


public class Consumer implements Port {
    private final int capacity;
    private final int processTime;

    private final Deque<Payload> buffer = new ArrayDeque<>();
    private Payload processing = null;
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
}
