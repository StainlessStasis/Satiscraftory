package io.github.stainlessstasis.manifold.client.factory_power;

import io.github.stainlessstasis.manifold.network.clientbound.PowerGridSyncPacket;

import java.util.List;

/**
 * Clientside mirror of the current dimension's power grid edges
 */
public final class ClientPowerGrid {
    private static volatile List<PowerGridSyncPacket.Entry> edges = List.of();

    private ClientPowerGrid() {}

    public static void applySync(List<PowerGridSyncPacket.Entry> newEdges) {
        edges = List.copyOf(newEdges);
    }

    public static List<PowerGridSyncPacket.Entry> getEdges() {
        return edges;
    }

    public static void clear() {
        edges = List.of();
    }
}