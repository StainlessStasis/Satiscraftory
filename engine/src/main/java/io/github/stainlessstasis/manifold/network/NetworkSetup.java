package io.github.stainlessstasis.manifold.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkSetup {
    public static final int NETWORK_VERSION = 1;

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(String.valueOf(NETWORK_VERSION));
        registrar.playToClient(BeltSyncPacket.TYPE, BeltSyncPacket.STREAM_CODEC, BeltSyncPacket::handleClient);
    }
}
