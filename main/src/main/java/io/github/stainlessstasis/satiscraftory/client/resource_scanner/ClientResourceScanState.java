package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import io.github.stainlessstasis.satiscraftory.network.clientbound.ResourceScanResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class ClientResourceScanState {
    public static final int RESULT_LIFETIME_TICKS = 20 * 60; // how long results stay visible before the bar stops showing them
    public static final double PING_SPEED_BLOCKS_PER_TICK = 500d / 20d;

    private static volatile @Nullable Identifier nodeTypeId = null;
    private static volatile List<ResourceScanResultPacket.ScannedNode> results = List.of();
    private static volatile long receivedAtTick = -1;
    private static volatile double originX = 0;
    private static volatile double originZ = 0;

    private ClientResourceScanState() {}

    public static void setResults(Identifier nodeTypeId, List<ResourceScanResultPacket.ScannedNode> results) {
        ClientResourceScanState.nodeTypeId = nodeTypeId;
        ClientResourceScanState.results = results;

        ClientLevel level = Minecraft.getInstance().level;
        ClientResourceScanState.receivedAtTick = level != null ? level.getGameTime() : 0;

        LocalPlayer player = Minecraft.getInstance().player;
        ClientResourceScanState.originX = player != null ? player.getX() : 0;
        ClientResourceScanState.originZ = player != null ? player.getZ() : 0;

        if (player != null) {
            ScanEffectRenderer.INSTANCE.ping(player.position());
        }
    }

    public static void clear() {
        nodeTypeId = null;
        results = List.of();
        receivedAtTick = -1;
    }

    public static @Nullable Identifier getNodeTypeId() {
        return nodeTypeId;
    }

    public static List<ResourceScanResultPacket.ScannedNode> getResults() {
        return results;
    }

    public static long getReceivedAtTick() {
        return receivedAtTick;
    }

    public static boolean isActive() {
        if (results.isEmpty()) return false;

        var level = Minecraft.getInstance().level;
        if (level == null) return false;

        return level.getGameTime() - receivedAtTick <= RESULT_LIFETIME_TICKS;
    }

    public static double getPingDelayTicks(ResourceScanResultPacket.ScannedNode node) {
        double dx = (node.pos().getX() + 0.5) - originX;
        double dz = (node.pos().getZ() + 0.5) - originZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        return distance / PING_SPEED_BLOCKS_PER_TICK;
    }
}