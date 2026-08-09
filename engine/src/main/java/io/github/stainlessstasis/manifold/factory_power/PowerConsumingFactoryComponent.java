package io.github.stainlessstasis.manifold.factory_power;

public interface PowerConsumingFactoryComponent extends PowerableFactoryComponent {
    void setPowered(boolean powered);
    boolean isPowered();
    void pauseForPowerLoss();
    void resumeFromPowerLoss();
}
