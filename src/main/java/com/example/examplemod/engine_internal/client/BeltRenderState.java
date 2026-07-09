package com.example.examplemod.engine_internal.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    Direction facing = Direction.NORTH;
    final List<BeltItemRenderData> items = new ArrayList<>();

    static final class BeltItemRenderData {
        final double position;
        final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();

        BeltItemRenderData(double position) {
            this.position = position;
        }
    }
}
