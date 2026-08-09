package io.github.stainlessstasis.manifold.factory_component.generator;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.factory_power.PowerProducingFactoryComponent;
import io.github.stainlessstasis.manifold.recipe.GeneratorFuel;
import io.github.stainlessstasis.manifold.recipe.ManifoldGeneratorFuels;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class Generator implements Port, PowerProducingFactoryComponent {
    private static final int MAX_CAPACITY = 64;

    private final Identifier generatorType;
    private final double powerRate;
    private final Scheduler scheduler;

    private Direction inputDirection;
    private @Nullable Identifier heldItemId;
    private @Nullable Identifier burningItemId;
    private int heldCount;
    private boolean burning;
    private long burnEndTick = -1;
    private long burnDurationTicks = -1;
    private Scheduler.@Nullable ScheduledTask burnTask;

    public Generator(Identifier generatorType, double powerRate, Scheduler scheduler) {
        this.generatorType = generatorType;
        this.powerRate = powerRate;
        this.scheduler = scheduler;
    }

    public static Generator restore(
            Identifier generatorType, double powerRate, Scheduler scheduler,
            @Nullable Identifier heldItemId, int heldCount, boolean burning, long burnEndTick,
            @Nullable Identifier burningItemId, long burnDurationTicks
    ) {
        Generator generator = new Generator(generatorType, powerRate, scheduler);
        generator.heldItemId = heldItemId;
        generator.heldCount = heldCount;
        generator.burning = burning;
        generator.burnEndTick = burnEndTick;
        generator.burningItemId = burningItemId;
        generator.burnDurationTicks = burnDurationTicks;

        if (burning) {
            generator.burnTask = scheduler.schedule(burnEndTick, generator::finishBurning);
        } else {
            generator.tryStartBurning();
        }
        return generator;
    }

    public void setInputDirection(Direction direction) {
        this.inputDirection = direction;
    }

    public boolean acceptsFrom(Direction direction) {
        return inputDirection == null || direction == inputDirection.getOpposite();
    }

    @Override
    public boolean canAccept(Payload payload) {
        if (heldItemId != null && !heldItemId.equals(payload.itemId())) return false;
        if (heldCount + payload.count() > capacityFor(payload.itemId())) return false;
        return ManifoldGeneratorFuels.isValidFuel(generatorType, payload.itemId());
    }

    @Override
    public void accept(Payload payload) {
        if (!canAccept(payload)) {
            throw new IllegalStateException("Generator at capacity or item mismatch, cannot accept " + payload);
        }
        if (heldItemId == null) heldItemId = payload.itemId();
        heldCount += payload.count();
        tryStartBurning();
    }

    public int tryExtractHeld(int amount) {
        int taken = Math.min(amount, heldCount);
        heldCount -= taken;
        if (heldCount == 0) heldItemId = null;
        return taken;
    }

    public void setHeldClientSide(@Nullable Identifier itemId, int amount) {
        this.heldItemId = itemId;
        this.heldCount = amount;
    }

    private void tryStartBurning() {
        if (burning || heldItemId == null || heldCount <= 0) return;

        GeneratorFuel fuel = ManifoldGeneratorFuels.get(generatorType, heldItemId);
        if (fuel == null) {
            heldItemId = null;
            heldCount = 0;
            return;
        }

        burningItemId = heldItemId;
        burnDurationTicks = fuel.burnTicks();

        heldCount--;
        burning = true;
        burnEndTick = scheduler.getCurrentTick() + fuel.burnTicks();
        burnTask = scheduler.schedule(burnEndTick, this::finishBurning);

        if (heldCount == 0) heldItemId = null;
    }

    private void finishBurning() {
        burnTask = null;
        burning = false;
        tryStartBurning();
    }

    public void cancelScheduledTask() {
        if (burnTask != null) {
            burnTask.cancel();
            burnTask = null;
        }
    }

    @Override
    public double getSupplyRate() {
        return burning ? powerRate : 0d;
    }

    @Override
    public void setOutputPort(int slot, Port port) {}

    @Override
    public int outputSlotCount() {
        return 0;
    }

    public Identifier getGeneratorType() {
        return generatorType;
    }

    public double getPowerRate() {
        return powerRate;
    }

    private static int capacityFor(Identifier itemId) {
        return Math.min(MAX_CAPACITY, ItemUtils.maxStackSizeFor(itemId));
    }

    public int getRoomFor(Identifier itemId) {
        if (heldItemId != null && !heldItemId.equals(itemId)) return 0;
        return capacityFor(itemId) - heldCount;
    }

    public int getCapacity() {
        return heldItemId != null ? capacityFor(heldItemId) : MAX_CAPACITY;
    }

    public @Nullable Identifier getHeldItemId() {
        return heldItemId;
    }

    public int getHeldCount() {
        return heldCount;
    }

    public boolean isBurning() {
        return burning;
    }

    public long getBurnEndTick() {
        return burnEndTick;
    }

    public @Nullable Identifier getBurningItemId() {
        return burningItemId;
    }

    public long getBurnDurationTicks() {
        return burnDurationTicks;
    }

    @Override
    public boolean isActivelyWorking() {
        return burning;
    }
}