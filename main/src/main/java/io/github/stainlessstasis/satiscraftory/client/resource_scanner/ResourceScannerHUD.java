package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.satiscraftory.client.HudColors;
import io.github.stainlessstasis.satiscraftory.network.clientbound.ResourceScanResultPacket;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.item.ResourceNodeScannerItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;

public final class ResourceScannerHUD implements GuiLayer {
    public static final String PATH = "resource_scanner_hud";

    private static final int BAR_WIDTH = 180;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_TOP_MARGIN = 8;
    private static final int BAR_EDGE_INSET = 8;

    private static final float FOV_DEGREES = 90f;

    private static final int MARKER_ICON_SIZE = 16;
    private static final int MARKER_GAP = 2;

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof ResourceNodeScannerItem)) return;
        if (!ClientResourceScanState.isActive()) return;

        var results = ClientResourceScanState.getResults();
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

        for (var node : results) {
            renderMarker(graphics, mc.font, node, markerIcon, playerX, playerZ, playerYaw, centerX, barX, barY);
        }
    }

    private void renderMarker(
            GuiGraphicsExtractor graphics, Font font, ResourceScanResultPacket.ScannedNode node, ItemStack icon,
            double playerX, double playerZ, float playerYaw, int centerX, int barX, int barY
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

        int iconX = Math.round(markerCenterX - MARKER_ICON_SIZE / 2f);
        int iconY = barY + BAR_HEIGHT + MARKER_GAP;
        graphics.item(icon, iconX, iconY);

        Component distanceText = Component.literal(Math.round(distance) + "m");
        int textColor = offScreen ? HudColors.LABEL_COLOR : HudColors.ACCENT_COLOR;
        GuiRenderUtils.centeredText(graphics, font, distanceText, Math.round(markerCenterX), iconY + MARKER_ICON_SIZE + 1, textColor);
    }
}