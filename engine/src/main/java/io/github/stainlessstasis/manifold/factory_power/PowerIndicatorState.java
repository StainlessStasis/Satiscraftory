package io.github.stainlessstasis.manifold.factory_power;

import java.awt.*;

public enum PowerIndicatorState {
    WORKING(new Color(0x00FF40)),       // has power, actively producing/crafting
    IDLE(new Color(0xFFD500)),          // has power, but not currently working (blocked outputs, full buffers, no recipe progressing, etc.)
    UNPOWERED(new Color(0xFF2020)),     // connected to the power grid, but it isn't receiving enough (or any) power
    NO_CONNECTION(new Color(0x202020)); // not connected to the power grid at all

    public final Color color;

    PowerIndicatorState(Color color) {
        this.color = color;
    }
}