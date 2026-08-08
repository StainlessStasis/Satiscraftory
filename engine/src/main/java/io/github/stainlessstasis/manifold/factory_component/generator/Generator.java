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

/**
 * A fuel-agnostic power producer: no fixed recipe, just "does this item burn, and for how long."
 * Modeled on {@link io.github.stainlessstasis.manifold.factory_component.consumer.Consumer} rather
 * than {@link io.github.stainlessstasis.manifold.factory_component.machine.Machine} - there's no
 * output, and what's "consumed" is whatever fuel-tagged item currently occupies the single stack,
 * not a fixed ingredient.
 * <p>
 * Mirrors a single inventory slot the way Satisfactory's biomass burner works: once an item starts
 * stacking up, the slot is pinned to that item type (any other item is rejected, i.e. jams the belt)
 * until the stack is fully burned through, at which point it opens back up to any valid fuel.
 * Capacity is the held item's own max stack size (capped at 64) - not a configurable field, since a
 * single inventory-style slot can never hold more of an item than the item itself allows.
 * <p>
 * Which items count as "valid fuel" and how long each burns is entirely data-driven via
 * {@link ManifoldGeneratorFuels}, keyed by this generator's {@code generatorType}.
 * <p>
 * Slot-facing getters/setters ({@link #tryExtractHeld}, {@link #setHeldClientSide}, etc.) mirror
 * {@link io.github.stainlessstasis.manifold.factory_component.machine.Machine}'s input-slot API so a
 * future menu can be built the same way {@code MachineMenu} was, without touching this class again.
 * The one asymmetry: Machine's slot item identity is always statically known (pinned by the recipe),
 * so its menu only ever needs to sync raw counts. A generator's held item is genuinely dynamic, so a
 * future {@code GeneratorMenu} will also need to sync {@link #getHeldItemId()} itself, not just amounts.
 */
public class Generator implements Port, PowerProducingFactoryComponent {
    private static final int MAX_CAPACITY = 64;

    private final Identifier generatorType;
    private final double powerRate;
    private final Scheduler scheduler;

    private Direction inputDirection;
    private @Nullable Identifier heldItemId;
    private int heldCount;
    private boolean burning;
    private long burnEndTick = -1;
    private Scheduler.@Nullable ScheduledTask burnTask;

    public Generator(Identifier generatorType, double powerRate, Scheduler scheduler) {
        this.generatorType = generatorType;
        this.powerRate = powerRate;
        this.scheduler = scheduler;
    }

    /**
     * Rebuilds a Generator from persisted state and re-schedules its in-flight burn (if any).
     */
    public static Generator restore(
            Identifier generatorType, double powerRate, Scheduler scheduler,
            @Nullable Identifier heldItemId, int heldCount, boolean burning, long burnEndTick
    ) {
        Generator generator = new Generator(generatorType, powerRate, scheduler);
        generator.heldItemId = heldItemId;
        generator.heldCount = heldCount;
        generator.burning = burning;
        generator.burnEndTick = burnEndTick;

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
        return inputDirection == null || direction == inputDirection;
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

    /**
     * GUI-facing take: removes up to {@code amount} from the held stack without touching an
     * in-flight burn (the currently-burning unit was already deducted from {@code heldCount} the
     * moment it started, so this can never interrupt a burn - it only ever pulls from the remainder).
     *
     * @return the amount actually removed
     */
    public int tryExtractHeld(int amount) {
        int taken = Math.min(amount, heldCount);
        heldCount -= taken;
        if (heldCount == 0) heldItemId = null;
        return taken;
    }

    /**
     * Client-side render sync only - mirrors {@code Machine.setInputAmountClientSide}. Unlike Machine,
     * a generator's held item isn't statically known from a recipe, so the item id has to be synced
     * too, not just the count.
     */
    public void setHeldClientSide(@Nullable Identifier itemId, int amount) {
        this.heldItemId = itemId;
        this.heldCount = amount;
    }

    private void tryStartBurning() {
        if (burning || heldItemId == null || heldCount <= 0) return;

        GeneratorFuel fuel = ManifoldGeneratorFuels.get(generatorType, heldItemId);
        if (fuel == null) {
            // Fuel table changed out from under us (e.g. datapack reload) - drop the now-invalid item
            // rather than get stuck holding something we can no longer burn.
            heldItemId = null;
            heldCount = 0;
            return;
        }

        heldCount--;
        burning = true;
        burnEndTick = scheduler.getCurrentTick() + fuel.burnTicks();
        burnTask = scheduler.schedule(burnEndTick, this::finishBurning);

        // The slot re-opens to a different fuel type as soon as this was the last unit of its kind,
        // even while that last unit is still actively burning - matches how Satisfactory reads "0 in
        // inventory" the instant a unit starts converting to power.
        if (heldCount == 0) heldItemId = null;
    }

    private void finishBurning() {
        burnTask = null;
        burning = false;
        tryStartBurning();
    }

    /**
     * Cancels the in-flight scheduled burn-completion callback. Call when this component is being
     * torn down (block removed) so the scheduler doesn't hold a dangling reference.
     */
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
    public void setOutputPort(int slot, Port port) {
        // Generators have no outputs - matches Consumer's no-op contract
    }

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

    /**
     * Max amount the currently-held item type can stack to (capped at 64). When the slot is empty
     * this can't be known yet - callers rendering an "empty slot" progress bar should treat this as
     * a placeholder upper bound, not a real limit.
     */
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
}
