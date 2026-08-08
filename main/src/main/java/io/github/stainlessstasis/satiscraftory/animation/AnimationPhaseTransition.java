package io.github.stainlessstasis.satiscraftory.animation;

public interface AnimationPhaseTransition {
    AnimationPhaseTransition NO_OP = new AnimationPhaseTransition() {};

    default void onEnterStartup(long gameTime) {}
    default void onEnterLoop(long gameTime) {}
    default void onEnterCooldown(long gameTime) {}
    default void onEnterIdle(long gameTime) {}
}