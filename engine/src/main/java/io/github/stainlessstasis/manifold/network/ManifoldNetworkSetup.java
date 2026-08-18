package io.github.stainlessstasis.manifold.network;

import io.github.stainlessstasis.manifold.network.clientbound.BeltSyncPacket;
import io.github.stainlessstasis.manifold.network.clientbound.ChainStateSyncPacket;
import io.github.stainlessstasis.manifold.network.clientbound.PowerGridSyncPacket;
import io.github.stainlessstasis.manifold.network.serverbound.SelectRecipePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ManifoldNetworkSetup {
    public static final int NETWORK_VERSION = 2;

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(String.valueOf(NETWORK_VERSION));
        registrar.playToClient(BeltSyncPacket.TYPE, BeltSyncPacket.STREAM_CODEC, BeltSyncPacket::handleClient);
        registrar.playToClient(PowerGridSyncPacket.TYPE, PowerGridSyncPacket.STREAM_CODEC, PowerGridSyncPacket::handleClient);
        registrar.playToClient(ChainStateSyncPacket.TYPE, ChainStateSyncPacket.STREAM_CODEC, ChainStateSyncPacket::handleClient);
        registrar.playToServer(SelectRecipePacket.TYPE, SelectRecipePacket.STREAM_CODEC, SelectRecipePacket::handleServer);
    }
}
