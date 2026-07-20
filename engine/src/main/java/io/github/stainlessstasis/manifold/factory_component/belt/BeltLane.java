package io.github.stainlessstasis.manifold.factory_component.belt;

import com.mojang.math.Constants;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeltLane implements Port {
    public static class BeltItem {
        private final long id;
        private final Payload payload;
        private double position;

        BeltItem(long id, Payload payload, double position) {
            this.id = id;
            this.payload = payload;
            this.position = position;
        }

        public long getId() { return id; }
        public Payload getPayload() { return payload; }
        public double getPosition() { return position; }
    }

    public record ItemSnapshot(long id, double position, Identifier itemId) {}

    private final UUID id;
    private final double speed;
    private final double minGap;

    private List<GlobalPos> blocks; // ordered, index 0 = lane head
    private double[] startDistance;
    private double totalLength;

    private final List<BeltItem> items = new ArrayList<>();
    private Port output = null;

    private long totalAccepted = 0;
    private long totalDischarged = 0;
    private long lastSyncedAccepted = 0;
    private long lastSyncedDischarged = 0;
    private boolean lastSyncedFrontJammed = false;
    private long lastSyncedTick = 0;
    private long lastSyncedItemCount = 0;
    private static final long MIN_TICKS_BETWEEN_SYNCS = 3;
    private long nextItemId = 0;

    public BeltLane(UUID id, List<GlobalPos> blocks, double speed, double minGap) {
        if (speed <= 0) throw new IllegalArgumentException("Speed must be positive");
        if (blocks.isEmpty()) throw new IllegalArgumentException("Lane must have at least one block");
        this.id = id;
        this.speed = speed;
        this.minGap = minGap;
        setBlocks(blocks);
    }

    private void setBlocks(List<GlobalPos> newBlocks) {
        this.blocks = new ArrayList<>(newBlocks);
        this.startDistance = new double[blocks.size() + 1];
        for (int i = 0; i <= blocks.size(); i++) {
            startDistance[i] = i;
        }
        this.totalLength = startDistance[blocks.size()];
    }

    public UUID getId() { return id; }
    public List<GlobalPos> getBlocks() { return blocks; }
    public int size() { return blocks.size(); }
    public double getTotalLength() { return totalLength; }
    public GlobalPos headBlock() { return blocks.getFirst(); }
    public GlobalPos tailBlock() { return blocks.getLast(); }

    /**
     * Find the index of a belt along the lane at the given position via binary search
     */
    public int beltIndexAt(double position) {
        double clamped = Math.clamp(position, 0, Math.max(totalLength - Constants.EPSILON, 0));
        int min = 0, max = blocks.size() - 1;
        while (min < max) {
            int mid = (min + max + 1) >>> 1;
            if (startDistance[mid] <= clamped) min = mid;
            else max = mid - 1;
        }
        return min;
    }

    /**
     * Local 0-1 progress within the belt at beltIndex, for a given lane-global position
     */
    public double localT(int blockIndex, double position) {
        double blockStart = startDistance[blockIndex];
        double blockLength = startDistance[blockIndex + 1] - blockStart;
        return blockLength <= 0 ? 0 : Math.clamp((position - blockStart) / blockLength, 0, 1);
    }

    public int indexOf(GlobalPos pos) {
        return blocks.indexOf(pos);
    }

    public void setOutput(Port output) { this.output = output; }
    public Port getOutput() { return output; }

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
        if (!canAccept(payload)) throw new IllegalStateException("Lane is jammed at entry");

        double insertPosition = Math.max(overflowAmount, 0);
        if (!items.isEmpty()) {
            double maxAllowed = items.getLast().position - minGap;
            insertPosition = Math.clamp(insertPosition, 0, Math.max(maxAllowed, 0));
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

        double previousClamped = totalLength;
        for (int index = 0; index < size; index++) {
            double cap = (index == 0) ? totalLength : Math.max(previousClamped - minGap, 0);
            previousClamped = Math.clamp(proposed[index], 0, cap);
            items.get(index).position = previousClamped;
        }

        BeltItem front = items.getFirst();
        if (front.position >= totalLength - Constants.EPSILON && output != null && output.canAccept(front.payload)) {
            double frontOvershoot = Math.max(proposed[0] - totalLength, 0);
            output.acceptWithOverflow(front.payload, frontOvershoot);
            items.removeFirst();
            totalDischarged++;

            double recomputedPrevious = totalLength;
            for (int index = 0; index < items.size(); index++) {
                double cap = (index == 0) ? totalLength : Math.max(recomputedPrevious - minGap, 0);
                recomputedPrevious = Math.clamp(proposed[index + 1], 0, cap);
                items.get(index).position = recomputedPrevious;
            }
        }
    }

    public boolean isFrontJammed() {
        if (items.isEmpty()) return false;
        BeltItem front = items.getFirst();
        if (front.position < totalLength - Constants.EPSILON) return false;
        return output == null || !output.canAccept(front.payload);
    }

    public record SplitResult(BeltLane before, BeltLane after, List<BeltItem> ejected) {}

    /**
     * Splits this lane by removing the block at blockIndex.
     * Blocks before it keep this lane's ID, and the output is left null/NO_ON_PORT.
     * Blocks after the split become their own new lane, inheriting this lane's output Port.
     */
    public SplitResult splitAt(int blockIndex, UUID afterLaneId) {
        if (blockIndex < 0 || blockIndex >= blocks.size()) {
            throw new IndexOutOfBoundsException(
                    "blockIndex " + blockIndex + " out of range for lane of size " + blocks.size()
            );
        }

        List<GlobalPos> beforeBlocks = blocks.subList(0, blockIndex);
        List<GlobalPos> afterBlocks = blocks.subList(blockIndex + 1, blocks.size());

        double removedStart = startDistance[blockIndex];
        double removedEnd = startDistance[blockIndex + 1];

        List<BeltItem> beforeItems = new ArrayList<>();
        List<BeltItem> ejected = new ArrayList<>();
        List<BeltItem> afterItems = new ArrayList<>();

        for (BeltItem item : items) {
            if (item.position < removedStart) {
                beforeItems.add(item);
            } else if (item.position < removedEnd) {
                ejected.add(item);
            } else {
                item.position -= removedEnd;
                afterItems.add(item);
            }
        }

        BeltLane before = null;
        if (!beforeBlocks.isEmpty()) {
            before = new BeltLane(this.id, beforeBlocks, speed, minGap);
            before.items.addAll(beforeItems);
            before.nextItemId = this.nextItemId;
            // this lane no longer has an output
        }

        BeltLane after = null;
        if (!afterBlocks.isEmpty()) {
            after = new BeltLane(afterLaneId, afterBlocks, speed, minGap);
            after.items.addAll(afterItems);
            after.nextItemId = this.nextItemId;
            after.output = this.output;
        }

        return new SplitResult(before, after, ejected);
    }

    /**
     * Merges the next lane onto the end of this lane, keeping this lane's ID.
     * Only valid when both lanes share the same speed.
     * Callers must also check the combined size against LaneManager.MAX_LANE_LENGTH before calling this.
     */
    public BeltLane mergeWith(BeltLane next) {
        if (Math.abs(this.speed - next.speed) > Constants.EPSILON) {
            throw new IllegalArgumentException("Cannot merge lanes with different speeds (" + this.speed + " vs " + next.speed + ")");
        }

        List<GlobalPos> mergedBlocks = new ArrayList<>(this.blocks.size() + next.blocks.size());
        mergedBlocks.addAll(this.blocks);
        mergedBlocks.addAll(next.blocks);

        BeltLane merged = new BeltLane(this.id, mergedBlocks, speed, minGap);
        merged.output = next.output;
        merged.nextItemId = Math.max(this.nextItemId, next.nextItemId);

        double offset = this.totalLength;
        merged.items.addAll(this.items);
        for (BeltItem item : next.items) {
            item.position += offset;
            merged.items.add(item);
        }

        merged.totalAccepted = this.totalAccepted + next.totalAccepted;
        merged.totalDischarged = this.totalDischarged + next.totalDischarged;

        return merged;
    }

    public BeltLane withBlockInserted(int index, GlobalPos block) {
        List<GlobalPos> newBlocks = new ArrayList<>(blocks.size() + 1);
        newBlocks.addAll(blocks.subList(0, index));
        newBlocks.add(block);
        newBlocks.addAll(blocks.subList(index, blocks.size()));

        BeltLane result = new BeltLane(this.id, newBlocks, speed, minGap);
        result.output = this.output;
        result.nextItemId = this.nextItemId;

        double insertionDistance = startDistance[index];
        for (BeltItem item : items) {
            BeltItem copy = new BeltItem(item.id, item.payload, item.position);
            if (copy.position >= insertionDistance) copy.position += 1.0;
            result.items.add(copy);
        }
        result.totalAccepted = this.totalAccepted;
        result.totalDischarged = this.totalDischarged;
        return result;
    }


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

    public long getLastSyncedTick() { return lastSyncedTick; }

    public int getItemCount() { return items.size(); }
    public double getSpeed() { return speed; }
    public double getMinGap() { return minGap; }
    public long getTotalAccepted() { return totalAccepted; }
    public long getTotalDischarged() { return totalDischarged; }

    public List<ItemSnapshot> getItemSnapshots() {
        List<ItemSnapshot> snapshots = new ArrayList<>(items.size());
        for (BeltItem item : items) snapshots.add(new ItemSnapshot(item.id, item.position, item.payload.itemId()));
        return snapshots;
    }

    public void restoreItem(Identifier itemId, double position, long id) {
        long resolvedId = (id >= 0) ? id : nextItemId;
        items.add(new BeltItem(resolvedId, new Payload(itemId), position));
        if (resolvedId >= nextItemId) nextItemId = resolvedId + 1;
    }
}