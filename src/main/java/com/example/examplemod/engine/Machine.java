package com.example.examplemod.engine;

import org.jetbrains.annotations.Nullable;

public class Machine implements Port {
    private final Recipe recipe;
    private final Scheduler scheduler;
    private Port output;

    private boolean crafting = false;
    private Payload pendingOutput = null;
    private long craftCompletionTick = -1;

    public Machine(Recipe recipe, Scheduler scheduler, Port output) {
        this(recipe, scheduler, output, false, null, -1);
    }

    private Machine(
            Recipe recipe, Scheduler scheduler, Port output, boolean crafting, @Nullable Payload pendingOutput, long craftCompletionTick)
    {
        if (output == null) throw new IllegalArgumentException("Machine needs an output Port");
        this.recipe = recipe;
        this.scheduler = scheduler;
        this.output = output;
        this.crafting = crafting;
        this.pendingOutput = pendingOutput;
        this.craftCompletionTick = craftCompletionTick;
    }

    public static Machine restore(
            Recipe recipe, Scheduler scheduler, Port output, boolean crafting, @Nullable Payload pendingOutput, long craftCompletionTick)
    {
        Machine machine = new Machine(recipe, scheduler, output, crafting, pendingOutput, craftCompletionTick);
        if (crafting) {
            scheduler.schedule(craftCompletionTick, machine::finishCrafting);
        }
        return machine;
    }

    public void setOutput(Port output) {
        this.output = output;
    }


    @Override
    public boolean canAccept(Payload payload) {
        return !crafting && pendingOutput == null && recipe.matchesInput(payload);
    }

    @Override
    public void accept(Payload payload) {
        if (!canAccept(payload)) {
            throw new IllegalStateException("Machine cannot accept this payload right now");
        }
        crafting = true;
        long completionTick = scheduler.getCurrentTick() + recipe.durationTicks();
        scheduler.schedule(completionTick, this::finishCrafting);
    }

    private void finishCrafting() {
        crafting = false;
        pendingOutput = recipe.craftOutput();
        tryFlushOutput();
    }

    public void tick(long currentTick) {
        tryFlushOutput();
    }

    private void tryFlushOutput() {
        if (pendingOutput != null && output.canAccept(pendingOutput)) {
            output.accept(pendingOutput);
            pendingOutput = null;
        }
    }

    public boolean isCrafting() {
        return crafting;
    }

    public boolean hasPendingOutput() {
        return pendingOutput != null;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public @Nullable String getPendingOutputTypeId() {
        return pendingOutput != null ? pendingOutput.typeId() : null;
    }

    public long getCraftCompletionTick() {
        return craftCompletionTick;
    }
}
