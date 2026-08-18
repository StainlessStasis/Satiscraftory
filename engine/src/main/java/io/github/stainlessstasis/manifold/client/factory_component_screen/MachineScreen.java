package io.github.stainlessstasis.manifold.client.factory_component_screen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.client.util.ScreenUtils;
import io.github.stainlessstasis.manifold.menu.machine.MachineMenu;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public class MachineScreen extends FactoryScreen<MachineMenu> {
    private static final int INGREDIENT_AMOUNT = 0xFFFFFFFF;
    private static final int HEADER_WIDTH = 64;
    private static final int HEADER_HEIGHT = 62;
    private static final int HEADER_Y = 6;
    private static final float ICON_SCALE = 1.5f;

    private static final int RECIPE_BUTTON_SIZE = 10;
    private static final int RECIPE_BUTTON_MARGIN = 2;

    private int recipeButtonX, recipeButtonY;
    private boolean recipeButtonHovered;

    public MachineScreen(MachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    protected int factorySlotCount() {
        return menu.getMachine().inputSlotCount() + menu.getMachine().outputSlotCount();
    }

    @Override
    protected void drawHeader(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        int headerX = x + (imageWidth - HEADER_WIDTH) / 2;
        int headerY = y + HEADER_Y;
        drawHeaderFrame(graphics, headerX, headerY, HEADER_WIDTH, HEADER_HEIGHT);

        RecipeIngredient mainOutput = menu.getMachine().getRecipe().mainOutput();
        ItemStack iconStack = new ItemStack(BuiltInRegistries.ITEM.getValue(mainOutput.itemId()));
        drawScaledItem(
                graphics, iconStack,
                headerX + HEADER_WIDTH / 2f,
                headerY + 2 + (8 * ICON_SCALE),
                ICON_SCALE
        );

        recipeButtonX = headerX + HEADER_WIDTH - RECIPE_BUTTON_SIZE - RECIPE_BUTTON_MARGIN;
        recipeButtonY = headerY + RECIPE_BUTTON_MARGIN;
        drawRecipeButton(graphics);

        int barX = headerX + 8;
        int barY = headerY + 30;
        int barWidth = HEADER_WIDTH - 16;
        drawProgressBar(graphics, barX, barY, barWidth);

        float progress = menu.getProgressFraction();
        String status;
        if (!menu.isPowered()) {
            status = "Unpowered";
        } else if (menu.isCrafting()) {
            status = String.format("Crafting - %.0f%%", progress * 100);
        } else {
            status = "Idle";
        }
        scaledCenteredText(
                graphics, font, Component.literal(status),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 4,
                HEADER_TEXT_COLOR, 0.7f
        );

        String powerText = String.format("%.1f / %.1f MW", menu.getCurrentPowerConsumptionMw(), menu.getRatedPowerConsumptionMw());
        scaledCenteredText(
                graphics, font, Component.literal(powerText),
                headerX + HEADER_WIDTH / 2,
                barY + PROGRESS_BAR_HEIGHT + 15,
                HEADER_TEXT_COLOR, 0.7f
        );

        if (recipeButtonHovered) {
            drawRecipeButtonTooltip(graphics);
        }
    }

    private void drawRecipeButton(GuiGraphicsExtractor graphics) {
        int bg = recipeButtonHovered ? ACCENT_COLOR_DARK : HEADER_BG_COLOR;
        graphics.fill(recipeButtonX, recipeButtonY, recipeButtonX + RECIPE_BUTTON_SIZE, recipeButtonY + RECIPE_BUTTON_SIZE, bg);
        GuiRenderUtils.border(graphics, recipeButtonX, recipeButtonY, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE, BORDER_COLOR);

        int lineColor = recipeButtonHovered ? HEADER_TEXT_COLOR : TEXT_COLOR_DIM;
        int inset = 2;
        int spacing = (RECIPE_BUTTON_SIZE - inset * 2) / 2;
        for (int i = 0; i < 3; i++) {
            int lineY = recipeButtonY + inset + i * spacing;
            graphics.fill(recipeButtonX + inset, lineY, recipeButtonX + RECIPE_BUTTON_SIZE - inset, lineY + 1, lineColor);
        }
    }

    private void drawRecipeButtonTooltip(GuiGraphicsExtractor graphics) {
        Component text = Component.translatable(Manifold.MODID+".recipe_menu.change_recipe");
        int textWidth = font.width(text);
        int boxX = recipeButtonX - textWidth - 6;
        int boxY = recipeButtonY;
        int boxWidth = textWidth + 8;
        int boxHeight = font.lineHeight + 6;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, OVERLAY_BG);
        GuiRenderUtils.text(graphics, font, text, boxX + 4, boxY + 3, HEADER_TEXT_COLOR);
    }

    @Override
    protected void drawFactorySlots(@NonNull GuiGraphicsExtractor graphics, int x, int y) {
        var machine = menu.getMachine();
        var recipe = machine.getRecipe();
        double craftsPerMinute = 1200d / recipe.durationTicks();

        int machineSlotCount = factorySlotCount();
        for (int i = 0; i < machineSlotCount; i++) {
            var slot = menu.slots.get(i);
            int slotX = x + slot.x - 1;
            int slotY = y + slot.y - 1;

            drawSlotBackground(graphics, slotX, slotY);

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
                    ACCENT_COLOR, 0.8f
            );
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && ScreenUtils.isInside(event.x(), event.y(), recipeButtonX, recipeButtonY, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE)) {
            MachineRecipe recipe = menu.getMachine().getRecipe();
            Minecraft.getInstance().setScreen(new RecipeSelectScreen(menu.getBlockPos(), recipe.machineType(), recipe.id(), this));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        recipeButtonHovered = ScreenUtils.isInside(mouseX, mouseY, recipeButtonX, recipeButtonY, RECIPE_BUTTON_SIZE, RECIPE_BUTTON_SIZE);
        super.mouseMoved(mouseX, mouseY);
    }
}