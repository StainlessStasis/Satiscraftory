package io.github.stainlessstasis.manifold.factory_power;

public interface PowerLinkable {
    default int getMaxPowerConnections() {
        return PowerGrid.DEFAULT_MAX_CONNECTIONS;
    }
}