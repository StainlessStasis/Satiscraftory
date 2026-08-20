package io.github.stainlessstasis.satiscraftory.building.lane;

import io.github.stainlessstasis.manifold.factory_component.Laneable;

/**
 * Whether the build gun places/demolishes {@link Laneable} buildings (currently only belts) one block at a time, or as a whole lane in a single action
 */
public enum LaneBuildMode {
    SINGLE,
    LANE,
    LANE_REVERSED;

    public LaneBuildMode toggled() {
        return switch (this) {
            case SINGLE -> LANE;
            case LANE -> LANE_REVERSED;
            case LANE_REVERSED -> SINGLE;
        };
    }

    public boolean isLane() {
        return this == LANE || this == LANE_REVERSED;
    }
}