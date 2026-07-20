package io.github.stainlessstasis.manifold.factory;

import com.mojang.math.Constants;
import io.github.stainlessstasis.manifold.factory_component.belt.BeltLane;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.factory_component.belt.LanePort;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LaneManager {
    public static final int MAX_LANE_LENGTH = 16;

    /**
     * Where a belt block sits within its lane
     */
    public record LaneReference(UUID laneId, int index) {}

    @FunctionalInterface
    public interface ItemDropHandler {
        void drop(GlobalPos removedBlockPos, List<BeltLane.BeltItem> ejectedItems);
    }

    private final Map<UUID, BeltLane> lanes = new HashMap<>();
    private final Map<GlobalPos, LaneReference> blockToLane = new HashMap<>();

    public @Nullable BeltLane getLane(UUID id) {
        return lanes.get(id);
    }

    public LaneManager.@Nullable LaneReference getReference(GlobalPos pos) {
        return blockToLane.get(pos);
    }

    public @Nullable BeltLane laneAt(GlobalPos pos) {
        LaneReference ref = blockToLane.get(pos);
        return ref == null ? null : lanes.get(ref.laneId());
    }

    public Map<UUID, BeltLane> getAllLanes() {
        return lanes;
    }

    public int getLaneCount() {
        return lanes.size();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isSameSpeed(double a, double b) {
        return Math.abs(a - b) <= Constants.EPSILON;
    }

    public boolean isTracked(GlobalPos pos) {
        return blockToLane.containsKey(pos);
    }

    private void registerLane(BeltLane lane) {
        lanes.put(lane.getId(), lane);
        List<GlobalPos> blocks = lane.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            blockToLane.put(blocks.get(i), new LaneReference(lane.getId(), i));
        }
    }

    private void unregisterLane(UUID id) {
        lanes.remove(id);
    }

    public void restoreLane(BeltLane lane) {
        registerLane(lane);
    }

    private boolean topologyUnchanged(GlobalPos pos, double blockSpeed, @Nullable GlobalPos upstreamNeighbor, @Nullable GlobalPos downstreamNeighbor) {
        LaneReference ref = blockToLane.get(pos);
        if (ref == null) return false;

        BeltLane lane = lanes.get(ref.laneId());
        if (lane == null || !isSameSpeed(lane.getSpeed(), blockSpeed)) return false;

        int index = ref.index();
        List<GlobalPos> blocks = lane.getBlocks();

        if (index > 0) {
            if (!blocks.get(index - 1).equals(upstreamNeighbor)) return false;
        } else if (upstreamNeighbor != null) {
            BeltLane upLane = laneAt(upstreamNeighbor);
            if (upLane != null && upstreamNeighbor.equals(upLane.tailBlock()) && isSameSpeed(upLane.getSpeed(), blockSpeed)) {
                boolean mergePossible = upLane.size() + lane.size() <= MAX_LANE_LENGTH;
                boolean alreadyBridged = upLane.getOutput() instanceof LanePort port && port.getPos().equals(pos);
                if (mergePossible && !alreadyBridged) return false;
            }
        }

        if (index < blocks.size() - 1) {
            return blocks.get(index + 1).equals(downstreamNeighbor);
        } else if (downstreamNeighbor != null) {
            BeltLane downLane = laneAt(downstreamNeighbor);
            if (downLane != null && downstreamNeighbor.equals(downLane.headBlock()) && isSameSpeed(downLane.getSpeed(), blockSpeed)) {
                boolean mergePossible = lane.size() + downLane.size() <= MAX_LANE_LENGTH;
                boolean alreadyBridged = lane.getOutput() instanceof LanePort port && port.getPos().equals(downstreamNeighbor);
                return !mergePossible || alreadyBridged;
            }
        }

        return true;
    }

    public BeltLane attachBlock(GlobalPos pos, double blockSpeed, double minGap,
                                @Nullable GlobalPos upstreamNeighbor, @Nullable GlobalPos downstreamNeighbor) {
        if (topologyUnchanged(pos, blockSpeed, upstreamNeighbor, downstreamNeighbor)) {
            return lanes.get(blockToLane.get(pos).laneId());
        }

        if (blockToLane.containsKey(pos)) {
            detachBlock(pos, FactoryNetwork.NO_OP_PORT, null);
        }

        BeltLane upLane = (upstreamNeighbor != null) ? laneAt(upstreamNeighbor) : null;
        if (upLane != null && !upstreamNeighbor.equals(upLane.tailBlock())) upLane = null;
        if (upLane != null && !isSameSpeed(upLane.getSpeed(), blockSpeed)) upLane = null;

        BeltLane downLane = (downstreamNeighbor != null) ? laneAt(downstreamNeighbor) : null;
        if (downLane != null && !downstreamNeighbor.equals(downLane.headBlock())) downLane = null;
        if (downLane != null && !isSameSpeed(downLane.getSpeed(), blockSpeed)) downLane = null;

        // guard against a placement that would loop a lane back onto itself
        if (upLane != null && downLane != null && upLane.getId().equals(downLane.getId())) {
            downLane = null;
        }

        if (upLane != null && downLane != null) {
            return bridgeTwoLanes(pos, upLane, downLane, blockSpeed, minGap);
        }
        if (upLane != null) {
            return extendTail(pos, upLane, blockSpeed, minGap);
        }
        if (downLane != null) {
            return extendHead(pos, downLane, blockSpeed, minGap);
        }

        // no same-speed neighbor to join; stand alone, but still link to whatever's there
        // (a different-speed neighbor, or nothing)
        BeltLane newLane = new BeltLane(UUID.randomUUID(), List.of(pos), blockSpeed, minGap);
        registerLane(newLane);

        BeltLane upstreamLane = (upstreamNeighbor != null) ? laneAt(upstreamNeighbor) : null;
        if (upstreamLane != null && upstreamNeighbor.equals(upstreamLane.tailBlock())) {
            upstreamLane.setOutput(new LanePort(this, pos));
        }

        BeltLane downstreamLane = (downstreamNeighbor != null) ? laneAt(downstreamNeighbor) : null;
        if (downstreamLane != null && downstreamNeighbor.equals(downstreamLane.headBlock())) {
            newLane.setOutput(new LanePort(this, downstreamNeighbor));
        }

        return newLane;
    }

    private BeltLane bridgeTwoLanes(GlobalPos pos, BeltLane upLane, BeltLane downLane, double blockSpeed, double minGap) {
        int combined = upLane.size() + 1 + downLane.size();

        if (combined <= MAX_LANE_LENGTH) {
            BeltLane merged = upLane.withBlockInserted(upLane.size(), pos).mergeWith(downLane);
            unregisterLane(upLane.getId());
            unregisterLane(downLane.getId());
            registerLane(merged);
            return merged;
        }

        // same speed, but over the length cap - extend whichever side has room and bridge the rest
        if (upLane.size() + 1 <= MAX_LANE_LENGTH) {
            BeltLane extended = upLane.withBlockInserted(upLane.size(), pos);
            extended.setOutput(new LanePort(this, downLane.headBlock()));
            unregisterLane(upLane.getId());
            registerLane(extended);
            return extended;
        }
        if (downLane.size() + 1 <= MAX_LANE_LENGTH) {
            BeltLane extended = downLane.withBlockInserted(0, pos);
            unregisterLane(downLane.getId());
            registerLane(extended);
            upLane.setOutput(new LanePort(this, pos));
            return extended;
        }

        // both lanes reached max length - pos becomes its own lane
        BeltLane standalone = new BeltLane(UUID.randomUUID(), List.of(pos), blockSpeed, minGap);
        standalone.setOutput(new LanePort(this, downLane.headBlock()));
        registerLane(standalone);
        upLane.setOutput(new LanePort(this, pos));
        return standalone;
    }

    private BeltLane extendTail(GlobalPos pos, BeltLane upLane, double blockSpeed, double minGap) {
        if (upLane.size() + 1 <= MAX_LANE_LENGTH) {
            BeltLane extended = upLane.withBlockInserted(upLane.size(), pos);
            unregisterLane(upLane.getId());
            registerLane(extended);
            return extended;
        }
        BeltLane standalone = new BeltLane(UUID.randomUUID(), List.of(pos), blockSpeed, minGap);
        registerLane(standalone);
        upLane.setOutput(new LanePort(this, pos));
        return standalone;
    }

    private BeltLane extendHead(GlobalPos pos, BeltLane downLane, double blockSpeed, double minGap) {
        if (downLane.size() + 1 <= MAX_LANE_LENGTH) {
            BeltLane extended = downLane.withBlockInserted(0, pos);
            unregisterLane(downLane.getId());
            registerLane(extended);
            return extended;
        }
        BeltLane standalone = new BeltLane(UUID.randomUUID(), List.of(pos), blockSpeed, minGap);
        standalone.setOutput(new LanePort(this, downLane.headBlock()));
        registerLane(standalone);
        return standalone;
    }


    @SuppressWarnings("UnusedReturnValue")
    public BeltLane.SplitResult detachBlock(GlobalPos pos, Port gapFillerPort, @Nullable ItemDropHandler dropHandler) {
        LaneReference ref = blockToLane.get(pos);
        if (ref == null) return new BeltLane.SplitResult(null, null, List.of());

        BeltLane lane = lanes.get(ref.laneId());
        BeltLane.SplitResult result = lane.splitAt(ref.index(), UUID.randomUUID());

        unregisterLane(lane.getId());
        blockToLane.remove(pos);

        if (result.before() != null) {
            result.before().setOutput(gapFillerPort);
            registerLane(result.before());
        }
        if (result.after() != null) {
            registerLane(result.after());
        }
        if (dropHandler != null && !result.ejected().isEmpty()) {
            dropHandler.drop(pos, result.ejected());
        }

        return result;
    }
}