package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.satiscraftory.client.HudColors;
import io.github.stainlessstasis.satiscraftory.network.clientbound.ResourceScanResultPacket;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.item.ResourceScannerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ResourceScannerHUD implements GuiLayer {
    public static final String PATH = "resource_scanner_hud";

    private static final int BAR_WIDTH = 180;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_TOP_MARGIN = 8;
    private static final int BAR_EDGE_INSET = 8;

    private static final float FOV_DEGREES = 90f;

    private static final int MARKER_ICON_SIZE = 16;
    private static final int MARKER_GAP = 2;

    /// Distance labels within this many px of each other on the bar collapse to just the closest node's
    private static final float LABEL_CLUSTER_RANGE = 26f;

    private List<ResourceScanResultPacket.ScannedNode> trackedResults = List.of();
    private final Set<BlockPos> pingedNodes = new HashSet<>();

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !(player.getMainHandItem().getItem() instanceof ResourceScannerItem)) return;
        if (!ClientResourceScanState.isActive()) return;

        var results = ClientResourceScanState.getResults();
        if (results != trackedResults) {
            trackedResults = results;
            pingedNodes.clear();
        }

        ResourceNodeType type = SCResourceNodes.byNodeId(ClientResourceScanState.getNodeTypeId());
        ItemStack markerIcon = new ItemStack(type != null ? type.getResourceBlock().asItem() : Items.BARRIER);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int centerX = screenWidth / 2;
        int barY = BAR_TOP_MARGIN;
        int barX = centerX - BAR_WIDTH / 2;

        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, HudColors.BAR_TRACK_BG);
        graphics.fill(centerX - 1, barY - 2, centerX + 1, barY + BAR_HEIGHT + 2, HudColors.ACCENT_COLOR);

        double playerX = player.getX();
        double playerZ = player.getZ();
        float playerYaw = player.getYRot();
        long elapsedTicks = mc.level.getGameTime() - ClientResourceScanState.getReceivedAtTick();

        List<MarkerPlacement> placements = new ArrayList<>(results.size());
        for (var node : results) {
            if (elapsedTicks < ClientResourceScanState.getPingDelayTicks(node)) continue;

            if (pingedNodes.add(node.pos())) {
                playPingSound();
            }

            placements.add(computePlacement(node, playerX, playerZ, playerYaw, centerX));
        }

        for (MarkerPlacement placement : placements) {
            renderMarker(graphics, mc.font, placement, placements, markerIcon, barY);
        }
    }

    private void playPingSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.25f));
    }

    private MarkerPlacement computePlacement(
            ResourceScanResultPacket.ScannedNode node, double playerX, double playerZ, float playerYaw, int centerX
    ) {
        BlockPos pos = node.pos();
        double dx = (pos.getX() + 0.5) - playerX;
        double dz = (pos.getZ() + 0.5) - playerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        // bearing from player to node (0 = south, -90 = east, 90 = west)
        double bearing = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double delta = Mth.wrapDegrees(bearing - playerYaw);

        boolean offScreen = Math.abs(delta) > FOV_DEGREES / 2f;
        double clampedDelta = Mth.clamp(delta, -FOV_DEGREES / 2f, FOV_DEGREES / 2f);

        float t = (float) (clampedDelta / (FOV_DEGREES / 2f)); // -1 (left edge) to 1 (right edge)
        float markerCenterX = centerX + t * (BAR_WIDTH / 2f - BAR_EDGE_INSET);

        return new MarkerPlacement(pos, distance, markerCenterX, offScreen);
    }

    private void renderMarker(
            GuiGraphicsExtractor graphics, Font font, MarkerPlacement placement, List<MarkerPlacement> allPlacements,
            ItemStack icon, int barY
    ) {
        int iconX = Math.round(placement.centerX() - MARKER_ICON_SIZE / 2f);
        int iconY = barY + BAR_HEIGHT + MARKER_GAP;
        graphics.item(icon, iconX, iconY);

        if (!isClosestInCluster(placement, allPlacements)) return;

        Component distanceText = Component.literal(Math.round(placement.distance()) + "m");
        int textColor = placement.offScreen() ? HudColors.LABEL_COLOR : HudColors.ACCENT_COLOR;
        GuiRenderUtils.centeredText(graphics, font, distanceText, Math.round(placement.centerX()), iconY + MARKER_ICON_SIZE + 1, textColor);
    }

    private boolean isClosestInCluster(MarkerPlacement placement, List<MarkerPlacement> allPlacements) {
        for (MarkerPlacement other : allPlacements) {
            if (other == placement) continue;
            if (Math.abs(other.centerX() - placement.centerX()) > LABEL_CLUSTER_RANGE) continue;

            if (other.distance() < placement.distance()) return false;
            // tiebreaker so two exactly equally distant nodes don't both defer to each other and draw nothing
            if (other.distance() == placement.distance() && System.identityHashCode(other) < System.identityHashCode(placement)) return false;
        }
        return true;
    }

    private record MarkerPlacement(BlockPos pos, double distance, float centerX, boolean offScreen) {
    }
}