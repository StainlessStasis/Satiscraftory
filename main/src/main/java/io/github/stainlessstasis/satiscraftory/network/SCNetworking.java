package io.github.stainlessstasis.satiscraftory.network;

import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public final class SCNetworking {
    public static final int NETWORK_VERSION = 1;

    private SCNetworking() {}

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(String.valueOf(NETWORK_VERSION));
        registrar.playToClient(
                ProgressionSyncPacket.TYPE, ProgressionSyncPacket.STREAM_CODEC,
                ProgressionSyncPacket::handleClient
        );
    }

    @SubscribeEvent
    static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TierUnlocks.syncToPlayer(player);
        }
    }
}