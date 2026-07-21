package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltShape;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    public BeltShape shape;
    public boolean reversed;
    public int cornerSegments;
    public float scrollOffset;
    public @Nullable BeltShape neighborBeforeShape;
    public @Nullable BeltShape neighborAfterShape;
    public final List<BeltItemRenderData> items = new ArrayList<>();

    public static class BeltItemRenderData {
        public long id;
        public double localT;
        public Identifier itemId;
        public final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }
}