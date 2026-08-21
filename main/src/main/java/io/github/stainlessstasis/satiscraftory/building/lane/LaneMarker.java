package io.github.stainlessstasis.satiscraftory.building.lane;

import io.github.stainlessstasis.satiscraftory.network.clientbound.LaneStartSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the first clicked position for belt lane placement, since placing lanes is a two-step process
 */
public final class LaneMarker {
    private static final Map<UUID, GlobalPos> MARKED_BY_PLAYER = new HashMap<>();
    private static final Map<UUID, Boolean> AXIS_HINT_BY_PLAYER = new HashMap<>();
    private static @Nullable BlockPos clientMarkedPos;

    private LaneMarker() {}

    public static @Nullable GlobalPos get(ServerPlayer player) {
        return MARKED_BY_PLAYER.get(player.getUUID());
    }

    public static void mark(ServerPlayer player, BlockPos pos) {
        GlobalPos markedPos = new GlobalPos(player.level().dimension(), pos);
        MARKED_BY_PLAYER.put(player.getUUID(), markedPos);
        AXIS_HINT_BY_PLAYER.remove(player.getUUID());
        sync(player, markedPos);
    }

    public static void clear(ServerPlayer player) {
        MARKED_BY_PLAYER.remove(player.getUUID());
        AXIS_HINT_BY_PLAYER.remove(player.getUUID());
        sync(player, null);
    }

    /**
     * Records which L-shaped route the client is currently previewing for the in-progress lane placement,
     * so the server placement/demolition can use the same route
     */
    public static void setAxisHint(ServerPlayer player, boolean primaryIsX) {
        if (!MARKED_BY_PLAYER.containsKey(player.getUUID())) return;
        AXIS_HINT_BY_PLAYER.put(player.getUUID(), primaryIsX);
    }

    public static @Nullable Boolean getAxisHint(ServerPlayer player) {
        return AXIS_HINT_BY_PLAYER.get(player.getUUID());
    }

    private static void sync(ServerPlayer player, @Nullable GlobalPos markedPos) {
        BlockPos pos = markedPos != null ? markedPos.pos() : null;
        PacketDistributor.sendToPlayer(player, new LaneStartSyncPacket(pos));
    }

    public static void applyClientSync(@Nullable BlockPos pos) {
        clientMarkedPos = pos;
    }

    public static @Nullable BlockPos getClientSide() {
        return clientMarkedPos;
    }
}