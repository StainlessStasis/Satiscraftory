package io.github.stainlessstasis.satiscraftory.progression;

import io.github.stainlessstasis.satiscraftory.network.clientbound.ProgressionSyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class TierUnlocks {
    private static int clientTier = 1;
    private static final Set<TierUnlock> CLIENT_UNLOCKS = EnumSet.noneOf(TierUnlock.class);

    private TierUnlocks() {}

    public static TierUnlockData server(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TierUnlockData.TYPE);
    }

    public static boolean isTierReachedOnServer(MinecraftServer server, int tier) {
        return tier == server(server).tier();
    }

    public static boolean isUnlockedOnServer(MinecraftServer server, TierUnlock unlock) {
        return server(server).isUnlocked(unlock);
    }

    public static void broadcastSync(MinecraftServer server) {
        TierUnlockData data = server(server);
        PacketDistributor.sendToAllPlayers(new ProgressionSyncPacket(data.tier(), List.copyOf(data.unlocks())));
    }

    public static void syncToPlayer(ServerPlayer player) {
        TierUnlockData data = server(player.level().getServer());
        PacketDistributor.sendToPlayer(player, new ProgressionSyncPacket(data.tier(), List.copyOf(data.unlocks())));
    }

    public static boolean isUnlockedOnClient(TierUnlock unlock) {
        return CLIENT_UNLOCKS.contains(unlock);
    }

    public static int clientTier() {
        return clientTier;
    }

    public static void applySync(int tier, Set<TierUnlock> unlocks) {
        clientTier = tier;
        CLIENT_UNLOCKS.clear();
        CLIENT_UNLOCKS.addAll(unlocks);
    }
}