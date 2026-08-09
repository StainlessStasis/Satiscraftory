package io.github.stainlessstasis.manifold.menu.machine;

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
    public void set(int index, int value) {}

    @Override
    public int getCount() {
        return MachineMenu.DATA_SIZE;
    }

    private int computeProgress() {
        if (machine.isStalled()) {
            return (int) Math.min(machine.getRecipe().durationTicks(), Integer.MAX_VALUE);
        }
        if (!machine.isCrafting()) return 0;

        long duration = machine.getRecipe().durationTicks();

        if (!machine.isPowered()) {
            long pausedRemaining = machine.getPausedRemainingTicks();
            long elapsed = pausedRemaining >= 0 ? duration - pausedRemaining : duration;
            return (int) Math.clamp(elapsed, 0, duration);
        }

        long currentTick = currentTickSupplier.getAsLong();
        long completionTick = machine.getCraftCompletionTick();
        long elapsed = duration - (completionTick - currentTick);
        return (int) Math.clamp(elapsed, 0, duration);
    }

    private int computeFlags() {
        int flags = 0;
        if (machine.isCrafting()) flags |= MachineMenu.FLAG_CRAFTING;
        if (machine.isStalled())  flags |= MachineMenu.FLAG_STALLED;
        if (machine.isPowered())  flags |= MachineMenu.FLAG_POWERED;
        return flags;
    }
}