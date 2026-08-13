package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import io.github.stainlessstasis.satiscraftory.network.clientbound.ResourceScanResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class ClientResourceScanState {
    public static final int RESULT_LIFETIME_TICKS = 20 * 60; // how long results stay visible before the bar stops showing them

    private static volatile @Nullable Identifier nodeTypeId = null;
    private static volatile List<ResourceScanResultPacket.ScannedNode> results = List.of();
    private static volatile long receivedAtTick = -1;

    private ClientResourceScanState() {}

    public static void setResults(Identifier nodeTypeId, List<ResourceScanResultPacket.ScannedNode> results) {
        ClientResourceScanState.nodeTypeId = nodeTypeId;
        ClientResourceScanState.results = results;

        var level = Minecraft.getInstance().level;
        ClientResourceScanState.receivedAtTick = level != null ? level.getGameTime() : 0;
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

    public static boolean isActive() {
        if (results.isEmpty()) return false;

        var level = Minecraft.getInstance().level;
        if (level == null) return false;

        return level.getGameTime() - receivedAtTick <= RESULT_LIFETIME_TICKS;
    }
}