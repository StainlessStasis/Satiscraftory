package io.github.stainlessstasis.satiscraftory.client.screen;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockPreviewPanel;
import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildingCatalog;
import io.github.stainlessstasis.satiscraftory.building.BuildingCategory;
import io.github.stainlessstasis.satiscraftory.network.SelectBuildingPacket;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildingSelectScreen extends Screen {
    private static final int PANEL_BG = 0xF0101010;
    private static final int TAB_BG = 0xFF2A2A2A;
    private static final int TAB_BG_SELECTED = 0xFF3D3D3D;
    private static final int GRID_CELL_BG = 0xFF262626;
    private static final int GRID_CELL_BG_HOVER = 0xFF3A3A3A;
    private static final int GRID_CELL_LOCKED_TINT = 0x90000000;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int TEXT_COLOR_DIM = 0xFF9A9A9A;

    private static final int TAB_HEIGHT = 22;
    private static final int CELL_SIZE = 36;
    private static final int CELL_PADDING = 4;
    private static final int GRID_COLUMNS = 5;
    private static final int PREVIEW_PANEL_WIDTH = 160;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int gridX;
    private int gridY;
    private int gridWidth;
    private int gridHeight;

    private BuildingCategory selectedCategory = BuildingCategory.values()[0];
    private int scrollOffset = 0;
    private BuildingCatalog.@Nullable BuildingEntry hoveredBuildingEntry = null;

    public BuildingSelectScreen() {
        super(Component.translatable(Satiscraftory.MODID+".build_menu.title"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(420, this.width - 40);
        panelHeight = Math.min(280, this.height - 40);
        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        gridX = panelX + 8;
        gridY = panelY + TAB_HEIGHT + 8;
        gridWidth = panelWidth - PREVIEW_PANEL_WIDTH - 16;
        gridHeight = panelHeight - TAB_HEIGHT - 16;
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BG);

        renderTabs(graphics, mouseX, mouseY);
        renderGrid(graphics, mouseX, mouseY);
        renderPreviewPanel(graphics);

        if (hoveredBuildingEntry != null) {
            renderTooltip(graphics, hoveredBuildingEntry, mouseX, mouseY);
        }

        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BuildingCategory[] categories = BuildingCategory.values();
        int tabWidth = gridWidth / categories.length;

        for (int i = 0; i < categories.length; i++) {
            BuildingCategory category = categories[i];
            int tabX = gridX + i * tabWidth;
            boolean isSelected = category == selectedCategory;
            boolean isHovered = isInside(mouseX, mouseY, tabX, panelY, tabWidth, TAB_HEIGHT);

            int bg = isSelected ? TAB_BG_SELECTED : (isHovered ? GRID_CELL_BG_HOVER : TAB_BG);
            graphics.fill(tabX, panelY, tabX + tabWidth, panelY + TAB_HEIGHT, bg);
            GuiRenderUtils.scaledCenteredText(graphics, this.font, category.displayName(), tabX + tabWidth / 2, panelY + 7, TEXT_COLOR, 0.8f);
        }
    }

    private void renderGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<BuildingCatalog.BuildingEntry> entries = BuildingCatalog.byCategory(selectedCategory);
        hoveredBuildingEntry = null;

        graphics.enableScissor(gridX, gridY, gridX + gridWidth, gridY + gridHeight);

        for (int i = 0; i < entries.size(); i++) {
            BuildingCatalog.BuildingEntry entry = entries.get(i);

            int col = i % GRID_COLUMNS;
            int row = i / GRID_COLUMNS;
            int cellX = gridX + col * (CELL_SIZE + CELL_PADDING);
            int cellY = gridY + row * (CELL_SIZE + CELL_PADDING) - scrollOffset;

            if (cellY + CELL_SIZE < gridY || cellY > gridY + gridHeight) continue;

            boolean isHovered = isInside(mouseX, mouseY, cellX, cellY, CELL_SIZE, CELL_SIZE)
                    && mouseY >= gridY && mouseY <= gridY + gridHeight;
            boolean unlocked = TierUnlocks.isUnlockedOnClient(entry.unlock());

            if (isHovered) hoveredBuildingEntry = entry;

            int bg = isHovered ? GRID_CELL_BG_HOVER : GRID_CELL_BG;
            graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, bg);

            ItemStack icon = new ItemStack(entry.blockItem());
            graphics.item(icon, cellX + (CELL_SIZE - 16) / 2, cellY + (CELL_SIZE - 16) / 2);

            if (!unlocked) {
                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, GRID_CELL_LOCKED_TINT);
            }
        }

        graphics.disableScissor();
    }

    private void renderPreviewPanel(GuiGraphicsExtractor graphics) {
        int previewX = gridX + gridWidth + 8;
        int previewY = panelY + TAB_HEIGHT + 8;
        int previewWidth = PREVIEW_PANEL_WIDTH - 8;
        int previewHeight = panelHeight - TAB_HEIGHT - 16;

        graphics.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, GRID_CELL_BG);

        BuildingCatalog.BuildingEntry buildingEntry = hoveredBuildingEntry;
        if (buildingEntry == null) return;

        int centerX = previewX + previewWidth / 2;
        int centerY = previewY + previewHeight / 2 - 10;
        long fakeGameTime = System.currentTimeMillis() / 50L;
        MultiblockPreviewPanel.render(graphics, buildingEntry.blockItem(), centerX, centerY, 64, fakeGameTime);

        GuiRenderUtils.centeredText(graphics, this.font, Component.translatable(buildingEntry.blockItem().getDescriptionId()),
                centerX, previewY + previewHeight - 24, TEXT_COLOR);
        GuiRenderUtils.centeredText(graphics, this.font, Component.translatable( Satiscraftory.MODID+".build_menu.tier", buildingEntry.tier()),
                centerX, previewY + previewHeight - 12, TEXT_COLOR_DIM);
    }

    private void renderTooltip(GuiGraphicsExtractor graphics, BuildingCatalog.BuildingEntry entry, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(entry.blockItem().getDescriptionId()));
        if (!TierUnlocks.isUnlockedOnClient(entry.unlock())) {
            lines.add(Component.translatable(Satiscraftory.MODID+".progression.locked", entry.tier()));
        }

        int textWidth = lines.stream().mapToInt(this.font::width).max().orElse(0);
        int boxWidth = textWidth + 8;
        int boxHeight = lines.size() * (this.font.lineHeight + 2) + 6;
        int boxX = mouseX + 12;
        int boxY = mouseY - 4;

        graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xF0100010);
        for (int i = 0; i < lines.size(); i++) {
            GuiRenderUtils.text(graphics, this.font, lines.get(i), boxX + 4, boxY + 4 + i * (this.font.lineHeight + 2), TEXT_COLOR);
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            BuildingCategory[] categories = BuildingCategory.values();
            int tabWidth = gridWidth / categories.length;
            for (int i = 0; i < categories.length; i++) {
                int tabX = gridX + i * tabWidth;
                if (isInside(event.x(), event.y(), tabX, panelY, tabWidth, TAB_HEIGHT)) {
                    selectedCategory = categories[i];
                    scrollOffset = 0;
                    return true;
                }
            }

            if (hoveredBuildingEntry != null) {
                selectBuilding(hoveredBuildingEntry);
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInside(mouseX, mouseY, gridX, gridY, gridWidth, gridHeight)) {
            int rows = (int) Math.ceil(BuildingCatalog.byCategory(selectedCategory).size() / (double) GRID_COLUMNS);
            int contentHeight = rows * (CELL_SIZE + CELL_PADDING);
            int maxScroll = Math.max(0, contentHeight - gridHeight);

            scrollOffset -= (int) (scrollY * (CELL_SIZE + CELL_PADDING) / 2);
            scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void selectBuilding(BuildingCatalog.BuildingEntry entry) {
        if (!TierUnlocks.isUnlockedOnClient(entry.unlock())) {
            return;
        }
        ClientPacketDistributor.sendToServer(new SelectBuildingPacket(entry.id()));
        this.onClose();
    }

    private static boolean isInside(double x, double y, int boxX, int boxY, int boxWidth, int boxHeight) {
        return x >= boxX && x < boxX + boxWidth && y >= boxY && y < boxY + boxHeight;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}