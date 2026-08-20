package io.github.stainlessstasis.satiscraftory.building.lane;

import io.github.stainlessstasis.manifold.factory_component.Laneable;

/**
 * Whether the build gun places/demolishes {@link Laneable} buildings (currently belts) one block at a time, or as a whole lane in a single action.
 */
public enum LaneBuildMode {
    SINGLE,
    LANE;

    public LaneBuildMode toggled() {
        return this == LANE ? SINGLE : LANE;
    }
}