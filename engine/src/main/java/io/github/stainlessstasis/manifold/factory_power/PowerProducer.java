package io.github.stainlessstasis.manifold.factory_power;

import io.github.stainlessstasis.manifold.factory_component.Port;

public class PowerProducer implements PowerProducingFactoryComponent {
    private final double supplyRate;
    private boolean active = true;

    public PowerProducer(double supplyRate) {
        this.supplyRate = supplyRate;
    }

    public static PowerProducer restore(double supplyRate, boolean active) {
        PowerProducer producer = new PowerProducer(supplyRate);
        producer.active = active;
        return producer;
    }

    @Override
    public double getSupplyRate() {
        return getEffectiveSupplyRate();
    }

    public double getRawSupplyRate() {
        return supplyRate;
    }

    public double getEffectiveSupplyRate() {
        return active ? supplyRate : 0d;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void setOutputPort(int slot, Port port) {}

    @Override
    public int outputSlotCount() {
        return 0;
    }
}