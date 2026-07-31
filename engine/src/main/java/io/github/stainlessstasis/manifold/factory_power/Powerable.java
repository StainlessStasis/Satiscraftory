package io.github.stainlessstasis.manifold.factory_power;

public interface Powerable {
    void setPowered(boolean powered);
    boolean isPowered();
    void pauseForPowerLoss();
    void resumeFromPowerLoss();
}
