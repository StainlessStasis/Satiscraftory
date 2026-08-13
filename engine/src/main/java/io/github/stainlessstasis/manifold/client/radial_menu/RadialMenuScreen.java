package io.github.stainlessstasis.manifold.client.radial_menu;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class RadialMenuScreen<T> extends Screen {
    private static final int RING_RADIUS = 56;
    private static final int ICON_SIZE = 16;
    private static final int SLOT_PADDING = 3;
    private static final int DEAD_ZONE_RADIUS = 20;
    private static final int CENTER_DOT_SIZE = 4;
    private static final int BACKDROP_COLOR = 0x66000000;
    private static final int LABEL_MARGIN = 14;

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

        graphics.fill(centerX - CENTER_DOT_SIZE / 2, centerY - CENTER_DOT_SIZE / 2,
                centerX + CENTER_DOT_SIZE / 2, centerY + CENTER_DOT_SIZE / 2, ACCENT_COLOR);

        for (int i = 0; i < options.size(); i++) {
            renderOption(graphics, i, i == hoveredIndex);
        }

        if (hoveredIndex >= 0) {
            Component label = options.get(hoveredIndex).label();
            GuiRenderUtils.centeredText(graphics, font, label, centerX, centerY + RING_RADIUS + LABEL_MARGIN, HEADER_TEXT_COLOR);
        }
    }

    private void renderOption(GuiGraphicsExtractor graphics, int index, boolean hovered) {
        RadialMenuOption<T> option = options.get(index);
        double angleDeg = index * (360d / options.size());
        double rad = Math.toRadians(angleDeg);

        int iconX = Math.round(centerX + (float) (Math.sin(rad) * RING_RADIUS)) - ICON_SIZE / 2;
        int iconY = Math.round(centerY - (float) (Math.cos(rad) * RING_RADIUS)) - ICON_SIZE / 2;

        int slotColor = hovered ? ACCENT_COLOR : SLOT_COLOR;
        graphics.fill(iconX - SLOT_PADDING, iconY - SLOT_PADDING,
                iconX + ICON_SIZE + SLOT_PADDING, iconY + ICON_SIZE + SLOT_PADDING, slotColor);
        graphics.fill(iconX - SLOT_PADDING + 1, iconY - SLOT_PADDING + 1,
                iconX + ICON_SIZE + SLOT_PADDING - 1, iconY + ICON_SIZE + SLOT_PADDING - 1, HEADER_BG_COLOR);

        graphics.item(option.icon(), iconX, iconY);
    }

    /**
     * @return -1 means no wedge is being hovered over
     */
    private int resolveHoveredIndex(double mouseX, double mouseY) {
        if (options.isEmpty()) return -1;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        if (Math.sqrt(dx * dx + dy * dy) < DEAD_ZONE_RADIUS) return -1;

        double angleDeg = Math.toDegrees(Math.atan2(dx, -dy)); // 0 = up, goes clockwise
        if (angleDeg < 0) angleDeg += 360;

        double sector = 360d / options.size();
        return (int) Math.floor((angleDeg + sector / 2) / sector) % options.size();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
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