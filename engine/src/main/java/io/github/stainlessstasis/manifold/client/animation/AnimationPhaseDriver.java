package io.github.stainlessstasis.manifold.client.animation;

import io.github.stainlessstasis.manifold.animation.AnimationPhase;
import io.github.stainlessstasis.manifold.animation.AnimationPhaseTransition;
import io.github.stainlessstasis.manifold.animation.PhasedAnimationStates;
import net.minecraft.client.animation.AnimationDefinition;

public class AnimationPhaseDriver {
    private final long startupLengthMs;
    private final long cooldownLengthMs;

    public AnimationPhaseDriver(AnimationDefinition startupDefinition, AnimationDefinition cooldownDefinition) {
        this.startupLengthMs = (long) (startupDefinition.lengthInSeconds() * 1000L);
        this.cooldownLengthMs = (long) (cooldownDefinition.lengthInSeconds() * 1000L);
    }

    public AnimationPhase tick(
            PhasedAnimationStates states, AnimationPhase currentPhase,
            long gameTime, float ageInTicks, boolean shouldRun,
            AnimationPhaseTransition transition
    ) {
        return switch (currentPhase) {
            case IDLE -> tickIdle(states, gameTime, shouldRun, transition);
            case STARTUP -> tickStartup(states, gameTime, ageInTicks, transition);
            case LOOP -> tickLoop(states, gameTime, shouldRun, transition);
            case COOLDOWN -> tickCooldown(states, gameTime, ageInTicks, transition);
        };
    }

    private AnimationPhase tickIdle(
            PhasedAnimationStates states, long gameTime, boolean shouldRun, AnimationPhaseTransition transition
    ) {
        if (!states.idle.isStarted()) {
            states.idle.start((int) gameTime);
        }
        if (!shouldRun) {
            return AnimationPhase.IDLE;
        }

        states.idle.stop();
        states.startup.start((int) gameTime);
        transition.onEnterStartup(gameTime);
        return AnimationPhase.STARTUP;
    }

    private AnimationPhase tickStartup(
            PhasedAnimationStates states, long gameTime, float ageInTicks, AnimationPhaseTransition transition
    ) {
        long elapsedMs = states.startup.getTimeInMillis(ageInTicks);
        if (elapsedMs < startupLengthMs) {
            return AnimationPhase.STARTUP;
        }

        states.startup.stop();
        states.loop.start((int) gameTime);
        transition.onEnterLoop(gameTime);
        return AnimationPhase.LOOP;
    }

    private AnimationPhase tickLoop(
            PhasedAnimationStates states, long gameTime, boolean shouldRun, AnimationPhaseTransition transition
    ) {
        if (shouldRun) {
            return AnimationPhase.LOOP;
        }

        states.loop.stop();
        states.cooldown.start((int) gameTime);
        transition.onEnterCooldown(gameTime);
        return AnimationPhase.COOLDOWN;
    }

    private AnimationPhase tickCooldown(
            PhasedAnimationStates states, long gameTime, float ageInTicks, AnimationPhaseTransition transition
    ) {
        long elapsedMs = states.cooldown.getTimeInMillis(ageInTicks);
        if (elapsedMs < cooldownLengthMs) {
            return AnimationPhase.COOLDOWN;
        }

        states.cooldown.stop();
        states.idle.start((int) gameTime);
        transition.onEnterIdle(gameTime);
        return AnimationPhase.IDLE;
    }
}