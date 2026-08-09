package io.github.stainlessstasis.manifold.menu;

public interface ProgressBar {
    /**
     * Progress fraction in [0, 1], used to render a progress bar in the corresponding screen
     */
    float getProgressFraction();
}