package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import io.github.stainlessstasis.manifold.factory_component.BeltLane;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    // this block's own belt strip
    public BeltShape shape;
    public boolean reversed;
    public int cornerSegments;
    public float scrollOffset;
    public BlockPos anchorPos;

    // item rendering for the entire belt lane
    public boolean isAnchor;
    public List<BlockPos> laneBlocks = List.of();
    public BeltShape[] blockShapes = new BeltShape[0];
    public boolean[] laneReversed = new boolean[0];
    public double totalLength = 0;

    public List<BeltLane.ItemSnapshot> cachedSyncedItems = null;
    public final List<BeltItemRenderData> items = new ArrayList<>();

    public static class BeltItemRenderData {
        public long id;
        public double position;
        public Identifier itemId;
        public final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }
}