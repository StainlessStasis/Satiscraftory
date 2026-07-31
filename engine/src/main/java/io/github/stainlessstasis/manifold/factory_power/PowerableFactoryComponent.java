package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory_component.FactoryComponent;

public interface PowerableFactoryComponent extends FactoryComponent {
    void setPowered(boolean powered);
    boolean isPowered();
    void pauseForPowerLoss();
    void resumeFromPowerLoss();
}
