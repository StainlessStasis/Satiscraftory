package io.github.stainlessstasis.manifold.client.screen;

import io.github.stainlessstasis.manifold.menu.machine.MachineMenu;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
}