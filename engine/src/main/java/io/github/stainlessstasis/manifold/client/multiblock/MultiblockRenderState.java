package io.github.stainlessstasis.manifold.client.multiblock;

import io.github.stainlessstasis.manifold.factory_power.PowerIndicatorState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class MultiblockRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public long gameTime = 0;
    public float ageInTicks = 0f;
    public PowerIndicatorState powerIndicatorState = PowerIndicatorState.NO_CONNECTION;
}