package io.github.stainlessstasis.manifold.menu.generator;

import io.github.stainlessstasis.manifold.factory_component.generator.Generator;
import net.minecraft.world.inventory.ContainerData;

import java.util.function.LongSupplier;

public class GeneratorContainerData implements ContainerData {
    private final Generator generator;
    private final LongSupplier currentTickSupplier;

    public GeneratorContainerData(Generator generator, LongSupplier currentTickSupplier) {
        this.generator = generator;
        this.currentTickSupplier = currentTickSupplier;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case GeneratorMenu.DATA_PROGRESS -> computeProgress();
            case GeneratorMenu.DATA_DURATION -> Math.clamp(generator.getBurnDurationTicks(), 0, Integer.MAX_VALUE);
            case GeneratorMenu.DATA_FLAGS    -> computeFlags();
            default -> throw new IllegalArgumentException("Unknown ContainerData index: " + index);
        };
    }

    @Override
    public void set(int index, int value) {}

    @Override
    public int getCount() {
        return GeneratorMenu.DATA_SIZE;
    }

    private int computeProgress() {
        if (!generator.isBurning()) return 0;

        long currentTick = currentTickSupplier.getAsLong();
        long completionTick = generator.getBurnEndTick();
        long duration = generator.getBurnDurationTicks();
        long elapsed = duration - (completionTick - currentTick);
        return Math.clamp(elapsed, 0, Integer.MAX_VALUE);
    }

    private int computeFlags() {
        int flags = 0;
        if (generator.isBurning()) flags |= GeneratorMenu.FLAG_BURNING;
        return flags;
    }
}