package io.github.stainlessstasis.manifold.factory_power;

public interface PowerProducingFactoryComponent extends PowerableFactoryComponent {
    double getSupplyRate();
}