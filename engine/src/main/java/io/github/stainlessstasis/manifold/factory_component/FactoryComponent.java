package io.github.stainlessstasis.manifold.factory_component;

public interface FactoryComponent {
    void setOutputPort(int slot, Port port);
    int outputSlotCount();
}

