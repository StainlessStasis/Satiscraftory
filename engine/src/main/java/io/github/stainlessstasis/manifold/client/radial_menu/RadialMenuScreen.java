package io.github.stainlessstasis.manifold.client.radial_menu;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class RadialMenuScreen<T> extends Screen {
    private static final int RING_INNER_RADIUS = 22;
    private static final int RING_OUTER_RADIUS = 64;
    private static final double WEDGE_GAP_DEGREES = 4;
    private static final int WEDGE_FILL_LINE_WIDTH = 1;
    private static final double WEDGE_STEP_DEGREES = 0.5;
    private static final int SEPARATOR_LINE_THICKNESS = 1;
    private static final int ICON_SIZE = 16;
    private static final int ICON_RADIUS = (RING_INNER_RADIUS + RING_OUTER_RADIUS) / 2;
    private static final int LABEL_MARGIN = 14;
    private static final int BACKDROP_COLOR = 0x66000000;

    private final List<RadialMenuOption<T>> options;
    private final Consumer<T> onSelect;

    private int centerX;
    private int centerY;
    private int hoveredIndex = -1;

    public RadialMenuScreen(List<RadialMenuOption<T>> options, Consumer<T> onSelect) {
        super(Component.empty());
        this.options = options;
        this.onSelect = onSelect;
    }

    public static <T> void open(List<RadialMenuOption<T>> options, Consumer<T> onSelect) {
        Minecraft.getInstance().setScreen(new RadialMenuScreen<>(options, onSelect));
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, BACKDROP_COLOR);

        hoveredIndex = resolveHoveredIndex(mouseX, mouseY);

        renderWedges(graphics);

        for (int i = 0; i < options.size(); i++) {
            renderIcon(graphics, i);
        }

        if (hoveredIndex >= 0) {
            Component label = options.get(hoveredIndex).label();
            GuiRenderUtils.centeredText(graphics, font, label, centerX, centerY + RING_OUTER_RADIUS + LABEL_MARGIN, HEADER_TEXT_COLOR);
        }
    }

    private void renderWedges(GuiGraphicsExtractor graphics) {
        int count = options.size();
        if (count == 0) return;

        double sector = 360d / count;
        double halfSector = sector / 2d;

        // wedge backgrounds
        for (int i = 0; i < count; i++) {
            double centerAngle = i * sector;
            double startAngle = centerAngle - halfSector + WEDGE_GAP_DEGREES / 2d;
            double endAngle = centerAngle + halfSector - WEDGE_GAP_DEGREES / 2d;
            int color = (i == hoveredIndex) ? ACCENT_COLOR : BORDER_COLOR;

            for (double angleDeg = startAngle; angleDeg <= endAngle; angleDeg += WEDGE_STEP_DEGREES) {
                drawSpoke(graphics, angleDeg, color, WEDGE_FILL_LINE_WIDTH);
            }

            drawSpoke(graphics, endAngle, color, WEDGE_FILL_LINE_WIDTH);
        }

        // separator lines
        for (int i = 0; i < count; i++) {
            double boundaryAngle = (i + 0.5) * sector;
            drawSpoke(graphics, boundaryAngle, ACCENT_COLOR_DARK, SEPARATOR_LINE_THICKNESS);
        }
    }

    private void drawSpoke(GuiGraphicsExtractor graphics, double angleDeg, int color, int halfThickness) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().rotate((float) Math.toRadians(angleDeg));

        graphics.fill(-halfThickness, -RING_OUTER_RADIUS, halfThickness, -RING_INNER_RADIUS, color);

        graphics.pose().popMatrix();
    }

    private void renderIcon(GuiGraphicsExtractor graphics, int index) {
        RadialMenuOption<T> option = options.get(index);
        double rad = Math.toRadians(index * (360d / options.size()));

        int iconX = Math.round(centerX + (float) (Math.sin(rad) * ICON_RADIUS)) - ICON_SIZE / 2;
        int iconY = Math.round(centerY - (float) (Math.cos(rad) * ICON_RADIUS)) - ICON_SIZE / 2;

        graphics.item(option.icon(), iconX, iconY);
    }

    /**
     * @return -1 means no wedge is being hovered over
     */
    private int resolveHoveredIndex(double mouseX, double mouseY) {
        if (options.isEmpty()) return -1;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distSqr = dx * dx + dy * dy;
        if (distSqr < RING_INNER_RADIUS * RING_INNER_RADIUS || distSqr > RING_OUTER_RADIUS * RING_OUTER_RADIUS) return -1;

        double angleDeg = angleFromCenter(dx, dy);
        int index = wedgeIndexFor(angleDeg, options.size());
        return isInGap(angleDeg, index, options.size()) ? -1 : index;
    }

    /**
     * @return 0 = up, increases clockwise
     */
    private static double angleFromCenter(double x, double y) {
        double angleDeg = Math.toDegrees(Math.atan2(x, -y));
        return angleDeg < 0 ? angleDeg + 360 : angleDeg;
    }

    private static int wedgeIndexFor(double angleDeg, int count) {
        double sector = 360d / count;
        int index = (int) Math.floor((angleDeg + sector / 2) / sector) % count;
        return index < 0 ? index + count : index;
    }

    private static boolean isInGap(double angleDeg, int index, int count) {
        double sector = 360d / count;
        double centerAngle = (index * sector) % 360;

        double diff = angleDeg - centerAngle;
        if (diff > 180) diff -= 360;
        if (diff < -180) diff += 360;

        return Math.abs(diff) > sector / 2 - WEDGE_GAP_DEGREES / 2;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == GLFW_MOUSE_BUTTON_LEFT && hoveredIndex >= 0 && hoveredIndex < options.size()) {
            T selected = options.get(hoveredIndex).value();
            onClose();
            onSelect.accept(selected);
            return true;
        }

        onClose();
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}