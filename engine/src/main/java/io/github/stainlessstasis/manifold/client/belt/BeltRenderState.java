package io.github.stainlessstasis.manifold.client.belt;

import io.github.stainlessstasis.manifold.factory_component.Belt;
import io.github.stainlessstasis.manifold.block.belt.BeltShape;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    BeltShape shape = BeltShape.NORTH_SOUTH;
    boolean reversed = false;
    List<Belt.ItemSnapshot> syncedItems = List.of();
    long syncTick = 0;

    final List<BeltItemRenderData> items = new ArrayList<>();

    boolean hideFrontItem = false;
    boolean itemIncomingActive = false;
    String itemIncomingTypeCached = null;
    final BeltItemRenderData itemIncoming = new BeltItemRenderData();
    BeltShape neighborShapeAtStart = null;
    BeltShape neighborShapeAtEnd = null;

    static final class BeltItemRenderData {
        double position;
        String typeId;
        final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }
}