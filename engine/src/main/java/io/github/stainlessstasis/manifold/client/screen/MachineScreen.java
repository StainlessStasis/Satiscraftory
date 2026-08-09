package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.machine.MachineMenu;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int BORDER_COLOR = 0xFF373737;
    private static final int SLOT_COLOR = 0xFF8B8B8B;

    private static final int HEADER_BG = 0xFF1C1C1C;
    private static final int ACCENT = 0xFFE69442;
    private static final int HEADER_TEXT = 0xFFE0E0E0;
    private static final int INGREDIENT_AMOUNT = 0xFFFFFFFF;
    private static final int EMPTY_SLOT_OVERLAY = 0xB0202020;

    private static final int HEADER_WIDTH = 64;
    private static final int HEADER_HEIGHT = 56;
    private static final int HEADER_Y = 6;
    private static final int PROGRESS_BAR_HEIGHT = 4;
    private static final float ICON_SCALE = 1.5f;

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

        drawHeader(graphics, x, y);
        drawMachineSlots(graphics, x, y);
        drawPlayerInventorySlots(graphics, x, y);
    }

    private void drawHeader(GuiGraphicsExtractor graphics, int x, int y) {
        int headerX = x + (imageWidth - HEADER_WIDTH) / 2;
        int headerY = y + HEADER_Y;

        graphics.fill(headerX, headerY, headerX + HEADER_WIDTH, headerY + HEADER_HEIGHT, HEADER_BG);
        drawBorder(graphics, headerX, headerY, HEADER_WIDTH, HEADER_HEIGHT, BORDER_COLOR);

        RecipeIngredient mainOutput = menu.getMachine().getRecipe().mainOutput();
        ItemStack iconStack = new ItemStack(BuiltInRegistries.ITEM.getValue(mainOutput.itemId()));
        drawScaledItem(
                graphics, iconStack,
                headerX + HEADER_WIDTH / 2f,
                headerY + 2 + (8 * ICON_SCALE),
                ICON_SCALE)
        ;

        int barX = headerX + 8;
        int barY = headerY + 32;
        int barWidth = HEADER_WIDTH - 16;
        graphics.fill(barX, barY, barX + barWidth, barY + PROGRESS_BAR_HEIGHT, BORDER_COLOR);
        float progress = menu.getProgressFraction();
        int filledWidth = Math.round(barWidth * progress);
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + PROGRESS_BAR_HEIGHT, ACCENT);
        }

        String status = menu.isCrafting() ? String.format("Crafting - %.0f%%", progress * 100)
                : menu.isStalled() ? "Stalled"
                : "Idle";
        scaledCenteredText(
                graphics, font, Component.literal(status),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 4
                , HEADER_TEXT, 0.7f
        );
    }

    private void drawScaledItem(GuiGraphicsExtractor graphics, ItemStack stack, float cx, float cy, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(scale, scale);
        graphics.item(stack, -8, -8);
        graphics.pose().popMatrix();
    }

    private void drawGhostItem(GuiGraphicsExtractor graphics, ItemStack stack, int slotX, int slotY) {
        graphics.item(stack, slotX + 1, slotY + 1);
        graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, EMPTY_SLOT_OVERLAY);
    }

    private void drawMachineSlots(GuiGraphicsExtractor graphics, int x, int y) {
        var machine = menu.getMachine();
        var recipe = machine.getRecipe();
        double craftsPerMinute = 1200d / recipe.durationTicks();

        int machineSlotCount = machine.inputSlotCount() + machine.outputSlotCount();
        for (int i = 0; i < machineSlotCount; i++) {
            var slot = menu.slots.get(i);
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;

            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_COLOR);
            drawBorder(graphics, slotX, slotY, 18, 18, BORDER_COLOR);

            boolean isInput = i < machine.inputSlotCount();
            int ingredientIndex = isInput ? i : i - machine.inputSlotCount();
            RecipeIngredient ingredient = isInput
                    ? recipe.inputs().get(ingredientIndex)
                    : recipe.outputs().get(ingredientIndex);

            if (!slot.hasItem()) {
                ItemStack ghostStack = new ItemStack(BuiltInRegistries.ITEM.getValue(ingredient.itemId()));
                drawGhostItem(graphics, ghostStack, slotX, slotY);
            }

            scaledCenteredText(
                    graphics, font, Component.literal("x" + ingredient.amount()),
                    slotX + 5,
                    slotY - 1,
                    INGREDIENT_AMOUNT, 0.7f
            );

            double perMinute = ingredient.amount() * craftsPerMinute;
            String rateText = formatRate(perMinute) + "/min";
            scaledCenteredText(
                    graphics, font, Component.literal(rateText),
                    slotX + 9,
                    slotY + 21,
                    ACCENT, 0.8f
            );
        }
    }

    private void drawPlayerInventorySlots(GuiGraphicsExtractor graphics, int x, int y) {
        var machine = menu.getMachine();
        int machineSlotCount = machine.inputSlotCount() + machine.outputSlotCount();

        for (int i = machineSlotCount; i < menu.slots.size(); i++) {
            var slot = menu.slots.get(i);
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, SLOT_COLOR);
            drawBorder(graphics, slotX, slotY, 18, 18, BORDER_COLOR);
        }
    }

    private String formatRate(double perMinute) {
        return (perMinute == Math.floor(perMinute))
                ? String.valueOf((int) perMinute)
                : String.format("%.1f", perMinute);
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