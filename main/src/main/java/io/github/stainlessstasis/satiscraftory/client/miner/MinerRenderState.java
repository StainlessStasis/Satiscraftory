package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AnimationState;

public class MinerRenderState extends MultiblockRenderState {
    public final AnimationState startupAnimationState = new AnimationState();
    public final AnimationState spinAnimationState = new AnimationState();
    Identifier resourceNodeId = SCResourceNodes.IRON.getNodeId();
}