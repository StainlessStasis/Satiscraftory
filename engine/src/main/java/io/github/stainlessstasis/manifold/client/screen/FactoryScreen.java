package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.ProgressBar;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public abstract class FactoryScreen<T extends AbstractContainerMenu & ProgressBar> extends AbstractContainerScreen<T> {
    protected static final int HEADER_Y = 6;
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
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(scale, scale);
        graphics.item(stack, -8, -8);
        graphics.pose().popMatrix();
    }

    protected void scaledCenteredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color, float scale) {
        var charSequence = text.getVisualOrderText();
        int width = font.width(charSequence);
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, y);
        graphics.pose().scale(scale, scale);
        graphics.text(font, charSequence, -(width + 1) / 2, 0, color, false);
        graphics.pose().popMatrix();
    }

    protected void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}