package io.github.stainlessstasis.satiscraftory.client.ui;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
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

import java.util.ArrayList;
import java.util.List;

public final class BuildGunHUD implements GuiLayer {
    public static final String PATH = "build_gun_hud";

    private static final int TITLE_PADDING_X = 10;
    private static final int TITLE_PADDING_Y = 5;
    private static final float LABEL_SCALE = 0.7f;
    private static final int LABEL_LINE_SPACING = 2;

    private static final int ITEM_SIZE = 16;
    private static final int ITEM_BOX_PADDING_X = 5;
    private static final int ITEM_BOX_PADDING_TOP = 4;
    private static final int ITEM_BOX_PADDING_BETWEEN = 2;
    private static final int ITEM_BOX_PADDING_BOTTOM = 3;
    private static final int ITEM_BOX_SPACING = 4;

    private static final int ROW_GAP = 4;
    private static final int BOTTOM_MARGIN = 48;

    private static final int TITLE_BOX_BG = 0xB0000000;
    private static final int LABEL_COLOR = 0xFFAAAAAA;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int ITEM_BOX_BG = 0xAA777777;
    private static final int SUFFICIENT_COLOR = 0xFF55FF55;
    private static final int INSUFFICIENT_COLOR = 0xFFFF5555;

    private record ItemBox(ItemStack icon, Component amountText, int color, int width) {}

    @Override
    public void render(@NonNull GuiGraphicsExtractor graphics, @NonNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        BlockItem selected = BuildGunItem.getSelectedBlockItem(player);
        BuildingCost cost = BuildGunItem.getSelectedBuildingCost(player);
        List<RecipeIngredient> inputs = cost != null ? cost.inputs() : List.of();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;

        List<ItemBox> itemBoxes = buildItemBoxes(mc.font, player, inputs);
        int itemBoxHeight = ITEM_BOX_PADDING_TOP + ITEM_SIZE + ITEM_BOX_PADDING_BETWEEN + mc.font.lineHeight + ITEM_BOX_PADDING_BOTTOM;

        int rowBottom = screenHeight - BOTTOM_MARGIN;
        int rowTop = itemBoxes.isEmpty() ? rowBottom : rowBottom - itemBoxHeight;
        int titleBoxBottom = itemBoxes.isEmpty() ? rowBottom : rowTop - ROW_GAP;

        renderTitleBox(graphics, mc.font, selected, centerX, titleBoxBottom);
        renderItemRow(graphics, mc.font, itemBoxes, centerX, rowTop, itemBoxHeight);
    }

    private void renderTitleBox(GuiGraphicsExtractor graphics, Font font, BlockItem selected, int centerX, int boxBottom) {
        Component label = Component.translatable(Satiscraftory.MODID +".build_gun.currently_building");
        Component name = Component.translatable(selected.getDescriptionId());

        int labelWidth = Math.round(font.width(label) * LABEL_SCALE);
        int nameWidth = font.width(name);
        int boxWidth = Math.max(labelWidth, nameWidth) + TITLE_PADDING_X * 2;

        int labelHeight = Math.round(font.lineHeight * LABEL_SCALE);
        int nameHeight = font.lineHeight;
        int boxHeight = TITLE_PADDING_Y * 2 + labelHeight + LABEL_LINE_SPACING + nameHeight;

        int boxX = centerX - boxWidth / 2;
        int boxTop = boxBottom - boxHeight;

        graphics.fill(boxX, boxTop, boxX + boxWidth, boxBottom, TITLE_BOX_BG);

        GuiRenderUtils.scaledCenteredText(graphics, font, label, centerX, boxTop + TITLE_PADDING_Y, LABEL_COLOR, LABEL_SCALE);
        GuiRenderUtils.centeredText(graphics, font, name, centerX, boxTop + TITLE_PADDING_Y + labelHeight + LABEL_LINE_SPACING, NAME_COLOR);
    }

    private List<ItemBox> buildItemBoxes(Font font, LocalPlayer player, List<RecipeIngredient> inputs) {
        List<ItemBox> boxes = new ArrayList<>(inputs.size());
        for (RecipeIngredient ingredient : inputs) {
            var itemOptional = BuiltInRegistries.ITEM.getOptional(ingredient.itemId());
            ItemStack iconStack = new ItemStack(itemOptional.orElse(Items.BARRIER));

            int held = BuildGunItem.countHeld(player.getInventory(), ingredient.itemId());
            boolean sufficient = held >= ingredient.amount();
            Component amountText = Component.literal(held + "/" + ingredient.amount());
            int color = sufficient ? SUFFICIENT_COLOR : INSUFFICIENT_COLOR;

            int textWidth = font.width(amountText);
            int width = Math.max(ITEM_SIZE, textWidth) + ITEM_BOX_PADDING_X * 2;

            boxes.add(new ItemBox(iconStack, amountText, color, width));
        }
        return boxes;
    }

    private void renderItemRow(GuiGraphicsExtractor graphics, Font font, List<ItemBox> itemBoxes, int centerX, int rowTop, int itemBoxHeight) {
        if (itemBoxes.isEmpty()) return;

        int rowWidth = itemBoxes.stream().mapToInt(ItemBox::width).sum()
                + (itemBoxes.size() - 1) * ITEM_BOX_SPACING;
        int boxX = centerX - rowWidth / 2;

        for (ItemBox box : itemBoxes) {
            int boxRight = boxX + box.width();
            graphics.fill(boxX, rowTop, boxRight, rowTop + itemBoxHeight, ITEM_BOX_BG);

            int iconX = boxX + (box.width() - ITEM_SIZE) / 2;
            int iconY = rowTop + ITEM_BOX_PADDING_TOP;
            graphics.item(box.icon(), iconX, iconY);

            int textY = iconY + ITEM_SIZE + ITEM_BOX_PADDING_BETWEEN;
            GuiRenderUtils.centeredText(graphics, font, box.amountText(), boxX + box.width() / 2, textY, box.color());

            boxX = boxRight + ITEM_BOX_SPACING;
        }
    }
}