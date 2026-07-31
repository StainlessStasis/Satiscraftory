package io.github.stainlessstasis.manifold.factory;

public interface Powerable {
    void setPowered(boolean powered);
    boolean isPowered();
    void pauseForPowerLoss();
    void resumeFromPowerLoss();
}
