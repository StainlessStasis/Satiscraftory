package io.github.stainlessstasis.manifold.client.multiblock.gui;

import io.github.stainlessstasis.manifold.client.util.GuiRenderUtils;
import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class MultiblockPreviewPanel {
    private MultiblockPreviewPanel() {}

    public static void render(GuiGraphicsExtractor graphics, BlockItem blockItem, int centerX, int centerY, int size, long gameTime) {
        if (blockItem.getBlock() instanceof Multiblock<?> multiblock && MultiblockGuiRenderer.rendererFor(multiblock) != null) {
            int half = size / 2;
            graphics.submitPictureInPictureRenderState(new MultiblockGuiPreviewRenderState(
                    multiblock, Direction.NORTH, gameTime, 0xFFFFFFFF,
                    centerX - half, centerY - half, centerX + half, centerY + half,
                    size, graphics.peekScissorStack()
            ));
        } else {
            renderItemIcon(graphics, blockItem, centerX, centerY, size);
        }
    }

    private static void renderItemIcon(GuiGraphicsExtractor graphics, BlockItem blockItem, int centerX, int centerY, int size) {
        ItemStack stack = new ItemStack(blockItem);
        float scale = size / 16f;
        GuiRenderUtils.scaledItem(graphics, stack, centerX, centerY, scale);
    }
}