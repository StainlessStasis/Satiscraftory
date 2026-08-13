package io.github.stainlessstasis.manifold.client.factory_component_screen;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.menu.ProgressBar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public abstract class FactoryScreen<T extends AbstractContainerMenu & ProgressBar> extends AbstractContainerScreen<@NonNull T> {
    protected static final int PROGRESS_BAR_HEIGHT = 4;
    protected static final int SLOT_SIZE = 18;

    protected FactoryScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, PANEL_COLOR);
        drawBorder(graphics, x, y, imageWidth, imageHeight, BORDER_COLOR);

        drawHeader(graphics, x, y);
        drawFactorySlots(graphics, x, y);
        drawSlotBackgrounds(graphics, x, y, factorySlotCount(), menu.slots.size());
    }

    protected abstract int factorySlotCount();
    protected abstract void drawHeader(GuiGraphicsExtractor graphics, int x, int y);
    protected abstract void drawFactorySlots(GuiGraphicsExtractor graphics, int x, int y);

    protected void drawHeaderFrame(GuiGraphicsExtractor graphics, int headerX, int headerY, int width, int height) {
        graphics.fill(headerX, headerY, headerX + width, headerY + height, HEADER_BG_COLOR);
        drawBorder(graphics, headerX, headerY, width, height, BORDER_COLOR);
    }

    protected void drawProgressBar(GuiGraphicsExtractor graphics, int barX, int barY, int barWidth) {
        float progress = menu.getProgressFraction();
        graphics.fill(barX, barY, barX + barWidth, barY + PROGRESS_BAR_HEIGHT, BORDER_COLOR);
        int filledWidth = Math.round(barWidth * progress);
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + PROGRESS_BAR_HEIGHT, ACCENT_COLOR);
        }
    }

    protected void drawSlotBackground(GuiGraphicsExtractor graphics, int slotX, int slotY) {
        graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, SLOT_COLOR);
        drawBorder(graphics, slotX, slotY, SLOT_SIZE, SLOT_SIZE, BORDER_COLOR);
    }

    protected void drawSlotBackgrounds(GuiGraphicsExtractor graphics, int x, int y, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            Slot slot = menu.slots.get(i);
            drawSlotBackground(graphics, x + slot.x - 1, y + slot.y - 1);
        }
    }

    protected void drawGhostItem(GuiGraphicsExtractor graphics, ItemStack stack, int slotX, int slotY) {
        graphics.item(stack, slotX + 1, slotY + 1);
        graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, EMPTY_SLOT_OVERLAY);
    }

    protected void drawScaledItem(GuiGraphicsExtractor graphics, ItemStack stack, float cx, float cy, float scale) {
        GuiRenderUtils.scaledItem(graphics, stack, cx, cy, scale);
    }

    protected void scaledCenteredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color, float scale) {
        GuiRenderUtils.scaledCenteredText(graphics, font, text, centerX, y, color, scale);
    }

    protected void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        GuiRenderUtils.border(graphics, x, y, width, height, color);
    }

    /**
     * Formats a per-minute rate as a whole number when it lands on an integer, or to one decimal place otherwise
     */
    protected String formatRate(double perMinute) {
        return (perMinute == Math.floor(perMinute))
                ? String.valueOf((int) perMinute)
                : String.format("%.1f", perMinute);
    }
}