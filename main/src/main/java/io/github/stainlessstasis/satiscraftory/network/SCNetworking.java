package io.github.stainlessstasis.satiscraftory.network;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.BuildingCosts;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.network.clientbound.*;
import io.github.stainlessstasis.satiscraftory.network.serverbound.DemolitionHoldPingPacket;
import io.github.stainlessstasis.satiscraftory.network.serverbound.SelectBuildingPacket;
import io.github.stainlessstasis.satiscraftory.network.serverbound.SelectScanTargetPacket;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@EventBusSubscriber(modid = Satiscraftory.MODID)
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

        registrar.playToServer(
                SelectBuildingPacket.TYPE, SelectBuildingPacket.STREAM_CODEC,
                SelectBuildingPacket::handleServer
        );
        registrar.playToServer(
                DemolitionHoldPingPacket.TYPE, DemolitionHoldPingPacket.STREAM_CODEC,
                DemolitionHoldPingPacket::handleServer
        );
        registrar.playToClient(
                DemolitionSelectionSyncPacket.TYPE, DemolitionSelectionSyncPacket.STREAM_CODEC,
                DemolitionSelectionSyncPacket::handleClient
        );

        registrar.playToServer(
                SelectScanTargetPacket.TYPE, SelectScanTargetPacket.STREAM_CODEC,
                SelectScanTargetPacket::handleServer
        );

        registrar.playToClient(
                ResourceScanResultPacket.TYPE, ResourceScanResultPacket.STREAM_CODEC,
                ResourceScanResultPacket::handleClient
        );

        registrar.playToClient(
                BuildingCostsSyncPacket.TYPE, BuildingCostsSyncPacket.STREAM_CODEC,
                BuildingCostsSyncPacket::handleClient
        );
        registrar.playToClient(
                SelectedBuildingSyncPacket.TYPE, SelectedBuildingSyncPacket.STREAM_CODEC,
                SelectedBuildingSyncPacket::handleClient
        );


    }

    @SubscribeEvent
    static void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TierUnlocks.syncToPlayer(player);
            PacketDistributor.sendToPlayer(player, new BuildingCostsSyncPacket(List.copyOf(BuildingCosts.allCosts().values())));
            BuildGunItem.syncSelection(player);

        }
    }

    @SubscribeEvent
    static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DemolitionSelectionManager.clear(player);
        }
    }
}