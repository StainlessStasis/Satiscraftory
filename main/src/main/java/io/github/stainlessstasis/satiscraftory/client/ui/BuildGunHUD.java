package io.github.stainlessstasis.satiscraftory.client.ui;

import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.item.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.recipe.BuildingCost;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.List;

public final class BuildGunHUD implements GuiLayer {
    public static final String PATH = "build_gun_hud";
    private static final int SLOT_SIZE = 24;
    private static final int SLOT_SPACING = 4;
    private static final int SLOT_MARGIN = 8;
    private static final int TITLE_HEIGHT = 16;
    private static final int BOX_BOTTOM_MARGIN = 26;

    private static final Color BOX_BG_COLOR = new Color(0x90000000, true);
    private static final Color TITLE_COLOR = new Color(0xFFFFFFFF, false);
    private static final Color SUFFICIENT_COLOR = new Color(0xFF55FF55, false);
    private static final Color INSUFFICIENT_COLOR = new Color(0xFFFF5555, false);

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        BlockItem selected = BuildGunItem.getSelectedBlockItem(player);
        BuildingCost cost = BuildGunItem.getSelectedBuildingCost(player);
        List<RecipeIngredient> inputs = cost != null ? cost.inputs() : List.of();

        int itemCount = Math.max(inputs.size(), 1);
        int boxWidth = Math.max(SLOT_MARGIN * 2 + itemCount * SLOT_SIZE + (itemCount - 1) * SLOT_SPACING, 120);
        int boxHeight = inputs.isEmpty() ? TITLE_HEIGHT + 10 : TITLE_HEIGHT + SLOT_SIZE + 16;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int boxX = (screenWidth - boxWidth) / 2;
        int boxY = screenHeight - BOX_BOTTOM_MARGIN - boxHeight;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, BOX_BG_COLOR.getRGB());

        Component title = Component.translatable(selected.getDescriptionId());
        centeredText(graphics, mc.font, title, boxX + boxWidth / 2, boxY + 10, TITLE_COLOR.getRGB());

        if (inputs.isEmpty()) return;

        int slotsY = boxY + TITLE_HEIGHT + 6;
        int slotX = boxX + SLOT_MARGIN;
        for (RecipeIngredient ingredient : inputs) {
            var itemOptional = BuiltInRegistries.ITEM.getOptional(ingredient.itemId());
            ItemStack iconStack = new ItemStack(itemOptional.orElse(Items.BARRIER));

            graphics.item(iconStack, slotX + (SLOT_SIZE - 16) / 2, slotsY);

            int held = BuildGunItem.countHeld(player.getInventory(), ingredient.itemId());
            boolean sufficient = held >= ingredient.amount();
            Component amountText = Component.literal(held + "/" + ingredient.amount());
            Color color = sufficient ? SUFFICIENT_COLOR : INSUFFICIENT_COLOR;
            centeredText(graphics, mc.font, amountText, slotX + SLOT_SIZE / 2, slotsY + 18, color.getRGB());

            slotX += SLOT_SIZE + SLOT_SPACING;
        }
    }

    private static void centeredText(GuiGraphicsExtractor graphics, Font font, Component text, int centerX, int y, int color) {
        int width = font.width(text);
        graphics.text(font, text, centerX - width / 2, y, color, true);
    }
}