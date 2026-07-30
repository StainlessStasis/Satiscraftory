package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.MachineMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.NonNull;

public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int BORDER_COLOR = 0xFF373737;
    private static final int SLOT_COLOR = 0xFF8B8B8B;
    private static final int TEXT_COLOR = 0xFF404040;

    public MachineScreen(MachineMenu menu, Inventory playerInventory, Component title) {
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

        for (var slot : menu.slots) {
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_COLOR);
            drawBorder(graphics, slotX, slotY, 18, 18, BORDER_COLOR);
        }

        float progress = menu.getProgressFraction();
        String status = menu.isStalled() ? "STALLED"
                : menu.isCrafting() ? String.format("%.0f%%", progress * 100)
                  : "IDLE";
        centeredTextNoShadow(graphics, font, status, x + imageWidth / 2, y + 20, TEXT_COLOR);
    }

    private void centeredTextNoShadow(GuiGraphicsExtractor graphics, Font font, String str, int x, int y, int color) {
        int width = font.width(str);
        graphics.text(font, str, x - (width + 1) / 2, y, color, false);
    }

    private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}