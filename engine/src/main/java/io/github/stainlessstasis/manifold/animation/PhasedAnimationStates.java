package io.github.stainlessstasis.manifold.animation;

import net.minecraft.world.entity.AnimationState;

public class PhasedAnimationStates {
    public final AnimationState idle = new AnimationState();
    public final AnimationState startup = new AnimationState();
    public final AnimationState loop = new AnimationState();
    public final AnimationState cooldown = new AnimationState();

    public void copyFrom(PhasedAnimationStates other) {
        idle.copyFrom(other.idle);
        startup.copyFrom(other.startup);
        loop.copyFrom(other.loop);
        cooldown.copyFrom(other.cooldown);
    }
}