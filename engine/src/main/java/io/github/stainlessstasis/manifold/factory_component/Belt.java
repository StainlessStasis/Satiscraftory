package io.github.stainlessstasis.manifold.factory_component;

import com.mojang.math.Constants;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Belt implements Port {
    private static class BeltItem {
        final long id;
        final Payload payload;
        double position; // 0 (just entered) to 1 (at exit)

        BeltItem(long id, Payload payload, double position) {
            this.id = id;
            this.payload = payload;
            this.position = position;
        }
    }

    public record ItemSnapshot(long id, double position, Identifier itemId) {}

    private final double speed;
    private final double minGap;
    private Port output;
    private long totalAccepted = 0;
    private long totalDischarged = 0;
    private long lastSyncedAccepted = 0;
    private long lastSyncedDischarged = 0;
    private boolean lastSyncedFrontJammed = false;
    private long lastSyncedTick = 0;
    private static final long MIN_TICKS_BETWEEN_SYNCS = 3;
    private long nextItemId = 0;

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
        items.add(new BeltItem(nextItemId++, payload, insertPosition));
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
        if (front.position >= 1 - Constants.EPSILON && output != null && output.canAccept(front.payload)) {
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

    public boolean isFrontJammed() {
        if (items.isEmpty()) return false;
        BeltItem front = items.getFirst();
        if (front.position < 1d - Constants.EPSILON) return false;
        return output == null || !output.canAccept(front.payload);
    }

    private long lastSyncedItemCount = 0;

    public boolean hasUnsyncedChanges(long currentTick) {
        boolean jamChanged = isFrontJammed() != lastSyncedFrontJammed;
        if (jamChanged) return true;

        boolean countsChanged = totalAccepted != lastSyncedAccepted || totalDischarged != lastSyncedDischarged;
        boolean itemCountChanged = items.size() != lastSyncedItemCount;
        if (countsChanged || itemCountChanged) return true;

        return currentTick - lastSyncedTick >= MIN_TICKS_BETWEEN_SYNCS;
    }

    public void markSynced(long tick) {
        lastSyncedAccepted = totalAccepted;
        lastSyncedDischarged = totalDischarged;
        lastSyncedTick = tick;
        lastSyncedItemCount = items.size();
        lastSyncedFrontJammed = isFrontJammed();
    }

    public long getLastSyncedTick() {
        return lastSyncedTick;
    }

    public Port getOutput() {
        return output;
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
        for (BeltItem beltItem : items) snapshots.add(new ItemSnapshot(beltItem.id, beltItem.position, beltItem.payload.itemId()));
        return snapshots;
    }

    public void restoreItem(Identifier itemId, double position, long id) {
        long resolvedId = (id >= 0) ? id : nextItemId;
        items.add(new BeltItem(resolvedId, new Payload(itemId), position));
        if (resolvedId >= nextItemId) nextItemId = resolvedId + 1;
    }
}