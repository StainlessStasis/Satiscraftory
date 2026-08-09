package io.github.stainlessstasis.satiscraftory.factory_component.miner;

import io.github.stainlessstasis.manifold.animation.PhasedAnimationStates;
import net.minecraft.world.entity.AnimationState;

public class MinerAnimationStates extends PhasedAnimationStates {
    public final AnimationState startupDescend = new AnimationState();
    public final AnimationState startupAlreadyDescended = new AnimationState();

    public void copyFrom(MinerAnimationStates other) {
        super.copyFrom(other);
        startupDescend.copyFrom(other.startupDescend);
        startupAlreadyDescended.copyFrom(other.startupAlreadyDescended);
    }
}