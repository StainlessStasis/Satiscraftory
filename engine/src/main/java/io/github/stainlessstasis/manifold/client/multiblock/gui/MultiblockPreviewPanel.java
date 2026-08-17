package io.github.stainlessstasis.manifold.client.multiblock.gui;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class MultiblockPreviewPanel {
    public static final float ITEM_SCALE = 64f;

    private MultiblockPreviewPanel() {}

    public static void render(
            GuiGraphicsExtractor graphics, BlockItem blockItem,
            int panelX, int panelY, int panelWidth, int panelHeight, long gameTime
    ) {
        int centerX = panelX + panelWidth / 2;
        int centerY = panelY + panelHeight / 2;

        if (blockItem.getBlock() instanceof Multiblock<?> multiblock && MultiblockGuiRenderer.rendererFor(multiblock) instanceof MultiblockRenderer<?,?> renderer) {
            graphics.submitPictureInPictureRenderState(new MultiblockGuiPreviewRenderState(
                    multiblock, Direction.NORTH, gameTime, 0xFFFFFFFF,
                    panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                    renderer.getModelGuiScale(), graphics.peekScissorStack()
            ));
        } else {
            renderItemIcon(graphics, blockItem, centerX, centerY, ITEM_SCALE);
        }
    }

    private static void renderItemIcon(GuiGraphicsExtractor graphics, BlockItem blockItem, int centerX, int centerY, float size) {
        ItemStack stack = new ItemStack(blockItem);
        float scale = size / 16f;
        GuiRenderUtils.scaledItem(graphics, stack, centerX, centerY, scale);
    }
}