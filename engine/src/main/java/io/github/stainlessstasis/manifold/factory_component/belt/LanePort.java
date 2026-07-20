package io.github.stainlessstasis.manifold.factory_component.belt;

import io.github.stainlessstasis.manifold.factory.LaneManager;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import net.minecraft.core.GlobalPos;

import java.util.Objects;

public class LanePort implements Port {
    private final LaneManager laneManager;
    private final GlobalPos pos;

    public LanePort(LaneManager laneManager, GlobalPos pos) {
        this.laneManager = laneManager;
        this.pos = pos;
    }

    public GlobalPos getPos() { return pos; }

    private BeltLane resolve() {
        return laneManager.laneAt(pos);
    }

    @Override
    public boolean canAccept(Payload payload) {
        BeltLane lane = resolve();
        return lane != null && lane.canAccept(payload);
    }

    @Override
    public void accept(Payload payload) {
        BeltLane lane = resolve();
        if (lane != null) lane.accept(payload);
    }

    @Override
    public void acceptWithOverflow(Payload payload, double overflowAmount) {
        BeltLane lane = resolve();
        if (lane != null) lane.acceptWithOverflow(payload, overflowAmount);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LanePort other && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}