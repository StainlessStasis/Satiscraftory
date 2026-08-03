package io.github.stainlessstasis.manifold.factory_power;

public class PowerProducer {
    private final double supplyRate;
    private boolean active = true;

    public PowerProducer(double supplyRate) {
        this.supplyRate = supplyRate;
    }

    public double getSupplyRate() {
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
}