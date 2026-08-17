package io.github.stainlessstasis.manifold.client.multiblock.gui;

import io.github.stainlessstasis.manifold.multiblock.Multiblock;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public record MultiblockGuiPreviewRenderState(
        Multiblock<?> multiblock, Direction facing, long gameTime, int tintColor,
        int x0, int y0, int x1, int y1, float scale,
        @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public MultiblockGuiPreviewRenderState(
            Multiblock<?> multiblock, Direction facing, long gameTime, int tintColor,
            int x0, int y0, int x1, int y1, float scale,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(
                multiblock, facing, gameTime, tintColor,
                x0, y0, x1, y1, scale, scissorArea,
                new ScreenRectangle(x0, y0, x1 - x0, y1 - y0)
        );
    }
}