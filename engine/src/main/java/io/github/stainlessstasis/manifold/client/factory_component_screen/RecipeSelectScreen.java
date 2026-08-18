package io.github.stainlessstasis.manifold.client.factory_component_screen;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.client.util.ScreenUtils;
import io.github.stainlessstasis.manifold.network.serverbound.SelectRecipePacket;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldMachineRecipes;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.github.stainlessstasis.manifold.menu.GuiColors.*;

public class RecipeSelectScreen extends Screen {
    private static final int TITLE_HEIGHT = 22;
    private static final int CELL_SIZE = 36;
    private static final int CELL_PADDING = 4;
    private static final int GRID_COLUMNS = 5;
    private static final int DETAIL_PANEL_WIDTH = 170;

    private final BlockPos machinePos;
    private final Identifier currentRecipeId;
    private final @Nullable Screen parentScreen;
    private final List<MachineRecipe> recipes;

    private int panelX, panelY, panelWidth, panelHeight;
    private int gridX, gridY, gridWidth, gridHeight;
    private int scrollOffset = 0;
    private @Nullable MachineRecipe hoveredRecipe = null;

    public RecipeSelectScreen(BlockPos machinePos, Identifier machineType, Identifier currentRecipeId, @Nullable Screen parentScreen) {
        super(Component.translatable(Manifold.MODID+".recipe_menu.title"));
        this.machinePos = machinePos;
        this.currentRecipeId = currentRecipeId;
        this.parentScreen = parentScreen;
        this.recipes = ManifoldMachineRecipes.recipesForMachineType(machineType);
    }

    @Override
    protected void init() {
        panelWidth = Math.min(420, this.width - 40);
        panelHeight = Math.min(280, this.height - 40);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        gridX = panelX + 8;
        gridY = panelY + TITLE_HEIGHT + 8;
        gridWidth = panelWidth - DETAIL_PANEL_WIDTH - 16;
        gridHeight = panelHeight - TITLE_HEIGHT - 16;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, OVERLAY_BG);
        GuiRenderUtils.centeredText(graphics, this.font, this.title, gridX + gridWidth / 2, panelY + 7, HEADER_TEXT_COLOR);

        renderGrid(graphics, mouseX, mouseY);
        MachineRecipe detail = hoveredRecipe != null ? hoveredRecipe : ManifoldMachineRecipes.get(currentRecipeId);
        renderDetailPanel(graphics, detail);

        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    private void renderGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        hoveredRecipe = null;
        graphics.enableScissor(gridX, gridY, gridX + gridWidth, gridY + gridHeight);

        for (int i = 0; i < recipes.size(); i++) {
            MachineRecipe recipe = recipes.get(i);
            int col = i % GRID_COLUMNS;
            int row = i / GRID_COLUMNS;
            int cellX = gridX + col * (CELL_SIZE + CELL_PADDING);
            int cellY = gridY + row * (CELL_SIZE + CELL_PADDING) - scrollOffset;

            if (cellY + CELL_SIZE < gridY || cellY > gridY + gridHeight) continue;

            boolean isHovered = ScreenUtils.isInside(mouseX, mouseY, cellX, cellY, CELL_SIZE, CELL_SIZE)
                    && mouseY >= gridY && mouseY <= gridY + gridHeight;
            boolean isSelected = recipe.id().equals(currentRecipeId);
            if (isHovered) hoveredRecipe = recipe;

            graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, isHovered ? GRID_CELL_BG_HOVER : GRID_CELL_BG);

            ItemStack icon = new ItemStack(BuiltInRegistries.ITEM.getValue(recipe.mainOutput().itemId()));
            graphics.item(icon, cellX + (CELL_SIZE - 16) / 2, cellY + (CELL_SIZE - 16) / 2);

            if (isSelected) GuiRenderUtils.border(graphics, cellX, cellY, CELL_SIZE, CELL_SIZE, ACCENT_COLOR);
        }

        graphics.disableScissor();
    }

    private void renderDetailPanel(GuiGraphicsExtractor graphics, @Nullable MachineRecipe recipe) {
        int px = gridX + gridWidth + 8;
        int py = panelY + TITLE_HEIGHT + 8;
        int pw = DETAIL_PANEL_WIDTH - 8;
        int ph = panelHeight - TITLE_HEIGHT - 16;

        graphics.fill(px, py, px + pw, py + ph, GRID_CELL_BG);
        if (recipe == null) return;

        int centerX = px + pw / 2;
        int y = py + 8;

        ItemStack mainIcon = new ItemStack(BuiltInRegistries.ITEM.getValue(recipe.mainOutput().itemId()));
        GuiRenderUtils.scaledItem(graphics, mainIcon, centerX, y + 16, 2f);
        y += 40;

        GuiRenderUtils.centeredText(graphics, this.font, Component.translatable(mainIcon.getItem().getDescriptionId()), centerX, y, HEADER_TEXT_COLOR);
        y += 12;

        double craftsPerMinute = 1200d / recipe.durationTicks();
        String durationText = String.format("%.1fs", recipe.durationTicks() / 20d);
        GuiRenderUtils.centeredText(graphics, this.font,
                Component.translatable(Manifold.MODID+".recipe_menu.duration", durationText), centerX, y, TEXT_COLOR_DIM);
        y += 14;

        y = renderIngredientList(graphics, Component.translatable(Manifold.MODID+".recipe_menu.inputs"), recipe.inputs(), px + 6, y, craftsPerMinute);
        renderIngredientList(graphics, Component.translatable(Manifold.MODID+".recipe_menu.outputs"), recipe.outputs(), px + 6, y, craftsPerMinute);
    }

    private int renderIngredientList(GuiGraphicsExtractor graphics, Component label, List<RecipeIngredient> ingredients, int x, int y, double craftsPerMinute) {
        if (ingredients.isEmpty()) return y;

        GuiRenderUtils.text(graphics, this.font, label, x, y, TEXT_COLOR_DIM);
        y += 11;

        for (RecipeIngredient ingredient : ingredients) {
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(ingredient.itemId()));
            graphics.item(stack, x, y);

            double perMinute = ingredient.amount() * craftsPerMinute;
            Component rateText = Component.translatable(
                    Manifold.MODID+".recipe_menu.ingredient_rate",
                    ingredient.amount(), FactoryScreen.formatRate(perMinute)
            );
            GuiRenderUtils.text(graphics, this.font, rateText, x + 20, y + 5, HEADER_TEXT_COLOR);
            y += 18;
        }
        return y + 4;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && hoveredRecipe != null) {
            if (!hoveredRecipe.id().equals(currentRecipeId)) {
                ClientPacketDistributor.sendToServer(new SelectRecipePacket(machinePos, hoveredRecipe.id()));
            }
            this.onClose();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (ScreenUtils.isInside(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
            int rows = (int) Math.ceil(recipes.size() / (double) GRID_COLUMNS);
            int maxScroll = Math.max(0, rows * (CELL_SIZE + CELL_PADDING) - gridHeight);
            scrollOffset = Math.clamp(scrollOffset - (int) (scrollY * (CELL_SIZE + CELL_PADDING) / 2), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}