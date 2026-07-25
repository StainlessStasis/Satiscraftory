package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import net.minecraft.world.entity.AnimationState;

public class MinerRenderState extends MultiblockRenderState {
    public final AnimationState startupAnimationState = new AnimationState();
    public final AnimationState spinAnimationState = new AnimationState();
}