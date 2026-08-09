package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.generator.GeneratorMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final int HEADER_WIDTH = 96;
    private static final int HEADER_HEIGHT = 40;
    private static final int HEADER_Y = 6;
    private static final int PROGRESS_BAR_HEIGHT = 4;

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_COLOR);
        drawBorder(graphics, x, y, imageWidth, imageHeight, BORDER_COLOR);

        drawHeader(graphics, x, y);
        drawFuelSlot(graphics, x, y);
        drawPlayerInventorySlots(graphics, x, y);
    }

    private void drawHeader(GuiGraphicsExtractor graphics, int x, int y) {
        int headerX = x + (imageWidth - HEADER_WIDTH) / 2;
        int headerY = y + HEADER_Y;

        graphics.fill(headerX, headerY, headerX + HEADER_WIDTH, headerY + HEADER_HEIGHT, HEADER_BG_COLOR);
        drawBorder(graphics, headerX, headerY, HEADER_WIDTH, HEADER_HEIGHT, BORDER_COLOR);

        float progress = menu.getProgressFraction();

        int barX = headerX + 8;
        int barY = headerY + 8;
        int barWidth = HEADER_WIDTH - 16;
        graphics.fill(barX, barY, barX + barWidth, barY + PROGRESS_BAR_HEIGHT, BORDER_COLOR);
        int filledWidth = Math.round(barWidth * progress);
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + PROGRESS_BAR_HEIGHT, ACCENT_COLOR);
        }

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

    private void drawFuelSlot(GuiGraphicsExtractor graphics, int x, int y) {
        var slot = menu.slots.getFirst();
        int slotX = x + slot.x - 1;
        int slotY = y + slot.y - 1;

        graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_COLOR);
        drawBorder(graphics, slotX, slotY, 18, 18, BORDER_COLOR);
    }

    private void drawPlayerInventorySlots(GuiGraphicsExtractor graphics, int x, int y) {
        for (int i = 1; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_COLOR);
            drawBorder(graphics, slotX, slotY, 18, 18, BORDER_COLOR);
        }
    }

    private void scaledCenteredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color, float scale) {
        var charSequence = text.getVisualOrderText();
        int width = font.width(charSequence);
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y);
        graphics.pose().scale(scale, scale);
        graphics.text(
                font, charSequence,
                -(width + 1) / 2,
                0, color, false
        );
        graphics.pose().popMatrix();
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}