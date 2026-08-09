package io.github.stainlessstasis.satiscraftory.menu.miner;

import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import net.minecraft.world.inventory.ContainerData;

import java.util.function.LongSupplier;

public class MinerContainerData implements ContainerData {
    private final Producer producer;
    private final LongSupplier currentTickSupplier;

    public MinerContainerData(Producer producer, LongSupplier currentTickSupplier) {
        this.producer = producer;
        this.currentTickSupplier = currentTickSupplier;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case MinerMenu.DATA_PROGRESS -> computeProgress();
            case MinerMenu.DATA_DURATION -> Math.clamp(producer.getInterval(), 0, Integer.MAX_VALUE);
            case MinerMenu.DATA_FLAGS    -> computeFlags();
            default -> throw new IllegalArgumentException("Unknown ContainerData index: " + index);
        };
    }

    @Override
    public void set(int index, int value) {}

    @Override
    public int getCount() {
        return MinerMenu.DATA_SIZE;
    }

    private int computeProgress() {
        if (!isActive()) return 0;

        long currentTick = currentTickSupplier.getAsLong();
        long nextTick = producer.getNextProductionTick();
        long interval = producer.getInterval();
        long elapsed = interval - (nextTick - currentTick);
        return Math.clamp(elapsed, 0, Integer.MAX_VALUE);
    }

    private boolean isActive() {
        return producer.isActivelyWorking();
    }

    private int computeFlags() {
        int flags = 0;
        if (isActive()) flags |= MinerMenu.FLAG_ACTIVE;
        if (producer.isPowered()) flags |= MinerMenu.FLAG_POWERED;
        return flags;
    }
}