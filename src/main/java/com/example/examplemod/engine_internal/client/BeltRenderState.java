package com.example.examplemod.engine_internal.client;

import com.example.examplemod.engine_internal.Belt;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class BeltRenderState extends BlockEntityRenderState {
    Direction facing = Direction.NORTH;
    List<Belt.ItemSnapshot> syncedItems = List.of();
    long syncTick = 0;
    boolean initialized = false;
    long accountedAccepted = 0;
    long accountedDischarged = 0;

    final List<BeltItemRenderData> items = new ArrayList<>();

    static final class BeltItemRenderData {
        double position;
        String typeId;
        final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
    }
}