package io.github.stainlessstasis.satiscraftory.building.lane;

import io.github.stainlessstasis.satiscraftory.network.clientbound.LaneBuildModeSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LaneBuildModeManager {
    private static final LaneBuildMode DEFAULT_MODE = LaneBuildMode.LANE;

    private static final Map<UUID, LaneBuildMode> MODE_BY_PLAYER = new HashMap<>();
    private static LaneBuildMode clientMode = DEFAULT_MODE;

    private LaneBuildModeManager() {}

    public static LaneBuildMode get(ServerPlayer player) {
        return MODE_BY_PLAYER.getOrDefault(player.getUUID(), DEFAULT_MODE);
    }

    public static void toggle(ServerPlayer player) {
        LaneBuildMode next = get(player).toggled();
        MODE_BY_PLAYER.put(player.getUUID(), next);
        sync(player, next);
        LaneMarker.clear(player);
    }

    public static void syncToPlayer(ServerPlayer player) {
        sync(player, get(player));
    }

    public static void clear(ServerPlayer player) {
        MODE_BY_PLAYER.remove(player.getUUID());
    }

    private static void sync(ServerPlayer player, LaneBuildMode mode) {
        PacketDistributor.sendToPlayer(player, new LaneBuildModeSyncPacket(mode));
    }

    public static void applyClientSync(LaneBuildMode mode) {
        clientMode = mode;
    }

    public static LaneBuildMode getClientSide() {
        return clientMode;
    }
}