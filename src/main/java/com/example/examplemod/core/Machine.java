package com.example.examplemod.core;

public class Machine implements Port {
    private final Recipe recipe;
    private final Scheduler scheduler;
    private final Port output;

    private boolean crafting = false;
    private Payload pendingOutput = null;

    public Machine(Recipe recipe, Scheduler scheduler, Port output) {
        this.recipe = recipe;
        this.scheduler = scheduler;
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
}
