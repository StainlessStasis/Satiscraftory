package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.generator.GeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public class GeneratorScreen extends FactoryScreen<GeneratorMenu> {
    private static final int HEADER_WIDTH = 96;
    private static final int HEADER_HEIGHT = 40;

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    protected int factorySlotCount() {
        return 1;
    }

    @Override
    protected void drawHeader(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        int headerX = x + (imageWidth - HEADER_WIDTH) / 2;
        int headerY = y + HEADER_Y;
        drawHeaderFrame(graphics, headerX, headerY, HEADER_WIDTH, HEADER_HEIGHT);

        int barX = headerX + 8;
        int barY = headerY + 8;
        int barWidth = HEADER_WIDTH - 16;
        drawProgressBar(graphics, barX, barY, barWidth);

        float progress = menu.getProgressFraction();
        String status = menu.isBurning() ? String.format("Burning - %.0f%%", progress * 100) : "Idle";
        scaledCenteredText(
                graphics, font, Component.literal(status),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 4,
                HEADER_TEXT_COLOR, 0.7f
        );

        String powerText = String.format("%.1f / %.1f MW", menu.getCurrentPowerOutputMw(), menu.getRatedPowerOutputMw());
        scaledCenteredText(
                graphics, font, Component.literal(powerText),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 15,
                HEADER_TEXT_COLOR, 0.7f
        );
    }

    @Override
    protected void drawFactorySlots(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        var slot = menu.slots.getFirst();
        drawSlotBackground(graphics, x + slot.x - 1, y + slot.y - 1);
    }
}