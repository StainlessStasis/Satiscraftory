package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory_component.FactoryComponent;

public interface PowerableFactoryComponent extends FactoryComponent {
    boolean isActivelyWorking();
}
