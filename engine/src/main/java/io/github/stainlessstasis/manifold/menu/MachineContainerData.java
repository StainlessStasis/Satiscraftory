package io.github.stainlessstasis.manifold.menu;

import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import net.minecraft.world.inventory.ContainerData;

import java.util.function.LongSupplier;

public class MachineContainerData implements ContainerData {

    private final Machine machine;
    private final LongSupplier currentTickSupplier;

    public MachineContainerData(Machine machine, LongSupplier currentTickSupplier) {
        this.machine = machine;
        this.currentTickSupplier = currentTickSupplier;
    }

    @Override
    public int get(int index) {
        return switch (index) {
            case MachineMenu.DATA_PROGRESS -> computeProgress();
            case MachineMenu.DATA_DURATION -> (int) Math.min(machine.getRecipe().durationTicks(), Integer.MAX_VALUE);
            case MachineMenu.DATA_FLAGS    -> computeFlags();
            default -> throw new IllegalArgumentException("Unknown ContainerData index: " + index);
        };
    }

    @Override
    public void set(int index, int value) {
        // no-op on server
    }

    @Override
    public int getCount() {
        return MachineMenu.DATA_SIZE;
    }

    private int computeProgress() {
        if (machine.isStalled()) {
            return (int) Math.min(machine.getRecipe().durationTicks(), Integer.MAX_VALUE);
        }
        if (!machine.isCrafting()) return 0;

        long currentTick = currentTickSupplier.getAsLong();
        long completionTick = machine.getCraftCompletionTick();
        long duration = machine.getRecipe().durationTicks();
        long elapsed = duration - (completionTick - currentTick);
        return Math.clamp(elapsed, 0, Integer.MAX_VALUE);
    }

    private int computeFlags() {
        int flags = 0;
        if (machine.isCrafting()) flags |= MachineMenu.FLAG_CRAFTING;
        if (machine.isStalled())  flags |= MachineMenu.FLAG_STALLED;
        return flags;
    }
}