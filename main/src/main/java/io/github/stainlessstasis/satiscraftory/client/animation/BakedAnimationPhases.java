package io.github.stainlessstasis.satiscraftory.client.animation;

import io.github.stainlessstasis.satiscraftory.animation.PhasedAnimationStates;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.geom.ModelPart;

public class BakedAnimationPhases {
    private final KeyframeAnimation idle;
    private final KeyframeAnimation startup;
    private final KeyframeAnimation loop;
    private final KeyframeAnimation cooldown;

    public BakedAnimationPhases(
            ModelPart root,
            AnimationDefinition idleDefinition,
            AnimationDefinition startupDefinition,
            AnimationDefinition loopDefinition,
            AnimationDefinition cooldownDefinition
    ) {
        this.idle = idleDefinition.bake(root);
        this.startup = startupDefinition.bake(root);
        this.loop = loopDefinition.bake(root);
        this.cooldown = cooldownDefinition.bake(root);
    }

    public void apply(PhasedAnimationStates states, float ageInTicks) {
        if (states.idle.isStarted()) {
            idle.apply(states.idle, ageInTicks);
        }
        if (states.startup.isStarted()) {
            startup.apply(states.startup, ageInTicks);
        }
        if (states.loop.isStarted()) {
            loop.apply(states.loop, ageInTicks);
        }
        if (states.cooldown.isStarted()) {
            cooldown.apply(states.cooldown, ageInTicks);
        }
    }
}