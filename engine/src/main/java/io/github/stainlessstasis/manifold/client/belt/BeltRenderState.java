package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.factory_component.Belt;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    BeltShape shape = BeltShape.NORTH_SOUTH;
    boolean reversed = false;
    List<Belt.ItemSnapshot> syncedItems = List.of();
    long syncTick = 0;
    float scrollOffset = 0f;

    final List<BeltItemRenderData> items = new ArrayList<>();

    boolean hideFrontItem = false;
    boolean itemIncomingActive = false;
    Identifier itemIncomingTypeCached = null;
    final BeltItemRenderData itemIncoming = new BeltItemRenderData();
    BeltShape neighborShapeAtStart = null;
    BeltShape neighborShapeAtEnd = null;

    static final class BeltItemRenderData {
        double position;
        Identifier itemId;
        final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }
}