package io.github.stainlessstasis.satiscraftory.building.demolition;

import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import io.github.stainlessstasis.satiscraftory.network.clientbound.DemolitionSelectionSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DemolitionSelectionManager {
    public static final int MAX_SELECTION = 50;
    public static final int HOLD_TICKS_TO_DEMOLISH = 20;

    private static final Map<UUID, LinkedHashSet<BlockPos>> SELECTIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> HOLD_PROGRESS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_PING_TICK = new ConcurrentHashMap<>();

    private static Set<BlockPos> clientSelection = Set.of();
    private static int clientHoldTicks = 0;

    private DemolitionSelectionManager() {}

    /**
     * Toggles a canonical building position in/out of the player's selection
     */
    public static boolean toggle(ServerPlayer player, BlockPos canonicalPos) {
        LinkedHashSet<BlockPos> selection = SELECTIONS.computeIfAbsent(player.getUUID(), _ -> new LinkedHashSet<>());

        if (selection.remove(canonicalPos)) {
            sync(player, selection);
            return true;
        }

        if (selection.size() >= MAX_SELECTION) {
            return false;
        }

        selection.add(canonicalPos);
        sync(player, selection);
        return true;
    }

    public static void onHoldPing(ServerPlayer player) {
        UUID id = player.getUUID();
        ServerLevel level = player.level();

        LinkedHashSet<BlockPos> selection = SELECTIONS.get(id);
        if (selection == null || selection.isEmpty()) return;

        long now = level.getGameTime();
        long lastPing = LAST_PING_TICK.getOrDefault(id, -1L);
        LAST_PING_TICK.put(id, now);

        int progress = (lastPing >= 0 && now - lastPing <= 2) ? HOLD_PROGRESS.getOrDefault(id, 0) + 1 : 1;

        if (progress >= HOLD_TICKS_TO_DEMOLISH) {
            HOLD_PROGRESS.remove(id);
            demolish(player, level, selection);
        } else {
            HOLD_PROGRESS.put(id, progress);
        }
    }

    private static void demolish(ServerPlayer player, ServerLevel level, LinkedHashSet<BlockPos> selection) {
        SELECTIONS.remove(player.getUUID());
        LAST_PING_TICK.remove(player.getUUID());

        boolean groupAsLane = LaneBuildModeManager.get(player).isLane();
        for (BlockPos pos : selection) {
            BuildGunDemolition.tryDemolish(player, level, pos, groupAsLane);
        }

        sync(player, Set.of());
    }

    public static void clear(ServerPlayer player) {
        UUID id = player.getUUID();
        SELECTIONS.remove(id);
        HOLD_PROGRESS.remove(id);
        LAST_PING_TICK.remove(id);
        sync(player, Set.of());
    }

    private static void sync(ServerPlayer player, Set<BlockPos> selection) {
        PacketDistributor.sendToPlayer(player, new DemolitionSelectionSyncPacket(List.copyOf(selection)));
    }

    public static Set<BlockPos> clientSelection() {
        return clientSelection;
    }

    public static void applyClientSync(List<BlockPos> selection) {
        clientSelection = Set.copyOf(selection);
    }

    public static int clientHoldTicks() {
        return clientHoldTicks;
    }

    public static void setClientHoldTicks(int ticks) {
        clientHoldTicks = ticks;
    }
}