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

    private final double speed;
    private final double minGap;
    private Port output;
    private long totalAccepted = 0;
    private long totalDischarged = 0;
    private long lastSyncedAccepted = 0;
    private long lastSyncedDischarged = 0;
    private long lastSyncedTick = 0;

    private final List<BeltItem> items = new ArrayList<>();

    public Belt(double speed, double minGap) {
        if (speed <= 0) throw new IllegalArgumentException("Speed must be positive");
        this.speed = speed;
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
        acceptWithOverflow(payload, 0);
    }

    @Override
    public void acceptWithOverflow(Payload payload, double overflowAmount) {
        if (!canAccept(payload)) throw new IllegalStateException("Belt is jammed at entry");

        double insertPosition = Math.max(overflowAmount, 0);
        if (!items.isEmpty()) {
            double maxAllowed = items.getLast().position - minGap;
            insertPosition = Math.clamp(maxAllowed, 0, insertPosition);
        }
        items.add(new BeltItem(payload, insertPosition));
        totalAccepted++;
    }

    public void tick(long currentTick) {
        int size = items.size();
        if (size == 0) return;

        double[] proposed = new double[size];
        for (int index = 0; index < size; index++) {
            proposed[index] = items.get(index).position + speed;
        }

        double previousClamped = 1;
        for (int index = 0; index < size; index++) {
            double cap = (index == 0) ? 1 : Math.max(previousClamped - minGap, 0);
            previousClamped = Math.clamp(proposed[index], 0, cap);
            items.get(index).position = previousClamped;
        }

        BeltItem front = items.getFirst();
        if (front.position >= 1 - 1.0e-6 && output != null && output.canAccept(front.payload)) {
            double frontOvershoot = Math.max(proposed[0] - 1, 0);
            output.acceptWithOverflow(front.payload, frontOvershoot);
            items.removeFirst();
            totalDischarged++;

            double recomputedPrevious = 1;
            for (int index = 0; index < items.size(); index++) {
                double cap = (index == 0) ? 1 : Math.max(recomputedPrevious - minGap, 0);
                recomputedPrevious = Math.clamp(proposed[index + 1], 0, cap);
                items.get(index).position = recomputedPrevious;
            }
        }
    }

    public boolean hasUnsyncedChanges() {
        return totalAccepted != lastSyncedAccepted || totalDischarged != lastSyncedDischarged;
    }

    public void markSynced(long tick) {
        lastSyncedAccepted = totalAccepted;
        lastSyncedDischarged = totalDischarged;
        lastSyncedTick = tick;
    }

    public long getLastSyncedTick() {
        return lastSyncedTick;
    }

    /**
     * True whenever the front item is sitting at the exit
     * (jammed waiting on the output, or about to discharge this same tick)
     **/
    public boolean isFrontAtExit() {
        return !items.isEmpty() && items.getFirst().position >= 1d;
    }

    public int getItemCount() {
        return items.size();
    }

    public List<Double> getPositions() {
        List<Double> positions = new ArrayList<>();
        for (BeltItem beltItem : items) positions.add(beltItem.position);
        return positions;
    }

    public double getSpeed() {
        return speed;
    }

    public double getMinGap() {
        return minGap;
    }

    public long getTotalAccepted() {
        return totalAccepted;
    }

    public long getTotalDischarged() {
        return totalDischarged;
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