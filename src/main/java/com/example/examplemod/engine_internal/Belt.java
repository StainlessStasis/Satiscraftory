package com.example.examplemod.engine_internal;

import java.util.ArrayList;
import java.util.List;


public class Belt implements Port {
    private static class BeltItem {
        final Payload payload;
        double position; // 0 (just entered) to 1 (at exit)

        BeltItem(Payload payload, double position) {
            this.payload = payload;
            this.position = position;
        }
    }

    public record ItemSnapshot(double position, String typeId) {}

    private final int lengthTicks;
    private final double speed;
    private final double minGap;
    private Port output;

    private final List<BeltItem> items = new ArrayList<>();

    public Belt(int lengthInTicks, double minGap) {
        if (lengthInTicks <= 0) throw new IllegalArgumentException("Length must be positive");
        this.lengthTicks = lengthInTicks;
        this.speed = 1d / lengthInTicks;
        this.minGap = minGap;
    }

    public void setOutput(Port output) {
        this.output = output;
    }

    @Override
    public boolean canAccept(Payload payload) {
        if (items.isEmpty()) return true;
        BeltItem back = items.getLast();
        return back.position >= minGap;
    }

    @Override
    public void accept(Payload payload) {
        if (!canAccept(payload)) throw new IllegalStateException("Belt is jammed at entry");
        items.add(new BeltItem(payload, 0));
    }

    public void tick(long currentTick) {
        for (int i = 0; i < items.size(); i++) {
            BeltItem beltItem = items.get(i);
            double cap = (i == 0) ? 1 : items.get(i - 1).position - minGap;
            double proposed = beltItem.position + speed;
            beltItem.position = Math.min(proposed, cap);
            if (beltItem.position < 0) beltItem.position = 0;
        }

        // try to discharge the front payload if it has reached the exit
        if (!items.isEmpty()) {
            BeltItem front = items.getFirst();
            if (front.position >= 1) {
                if (output != null && output.canAccept(front.payload)) {
                    output.accept(front.payload);
                    items.removeFirst();
                }
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public List<Double> getPositions() {
        List<Double> positions = new ArrayList<>();
        for (BeltItem beltItem : items) positions.add(beltItem.position);
        return positions;
    }

    public int getLengthTicks() {
        return lengthTicks;
    }

    public double getMinGap() {
        return minGap;
    }

    public List<ItemSnapshot> getItemSnapshots() {
        List<ItemSnapshot> snapshots = new ArrayList<>();
        for (BeltItem beltItem : items) snapshots.add(new ItemSnapshot(beltItem.position, beltItem.payload.typeId()));
        return snapshots;
    }

    public void restoreItem(String typeId, double position) {
        items.add(new BeltItem(new Payload(typeId), position));
    }
}

