package io.github.stainlessstasis.manifold.factory_component;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Machine {
    private MachineRecipe recipe;
    private final Scheduler scheduler;
    private final int bufferMultiplier; // how many recipe batches worth of input each slot can hold at once
    private int[] bufferedCounts; // per input slot, how many items are currently buffered
    private final List<Deque<Payload>> pendingOutputs = new ArrayList<>(); // per output slot, items waiting to leave
    private final List<Port> outputPorts = new ArrayList<>();
    private final Map<Direction, Integer> inputFaces = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer> outputFaces = new EnumMap<>(Direction.class);
    private boolean crafting = false;
    private long craftCompletionTick = -1;

    public Machine(MachineRecipe recipe, Scheduler scheduler, List<Port> initialOutputPorts) {
        this(recipe, scheduler, initialOutputPorts, 8);
    }

    public Machine(MachineRecipe recipe, Scheduler scheduler, List<Port> initialOutputPorts, int bufferMultiplier) {
        if (initialOutputPorts.size() != recipe.outputCount()) {
            throw new IllegalArgumentException("Expected " + recipe.outputCount() + " output ports, got " + initialOutputPorts.size());
        }
        this.recipe = recipe;
        this.scheduler = scheduler;
        this.bufferMultiplier = bufferMultiplier;
        this.bufferedCounts = new int[recipe.inputCount()];
        for (int i = 0; i < recipe.outputCount(); i++) pendingOutputs.add(new ArrayDeque<>());
        this.outputPorts.addAll(initialOutputPorts);
    }

    public static Machine restore(
            MachineRecipe recipe, Scheduler scheduler, List<Port> outputPorts,
            int bufferMultiplier, boolean crafting, long craftCompletionTick,
            int[] bufferedCounts, List<List<Identifier>> pendingOutputItemIds,
            Map<Direction, Integer> inputFaces, Map<Direction, Integer> outputFaces
    ) {
        Machine machine = new Machine(recipe, scheduler, outputPorts, bufferMultiplier);
        machine.crafting = crafting;
        machine.craftCompletionTick = craftCompletionTick;
        machine.bufferedCounts = bufferedCounts;

        for (var entry : inputFaces.entrySet()) {
            machine.assignInputFace(entry.getKey(), entry.getValue());
        }
        for (var entry : outputFaces.entrySet()) {
            machine.assignOutputFace(entry.getKey(), entry.getValue());
        }

        machine.pendingOutputs.clear();
        for (List<Identifier> itemIds : pendingOutputItemIds) {
            Deque<Payload> queue = new ArrayDeque<>();
            for (Identifier itemId : itemIds) queue.addLast(new Payload(itemId));
            machine.pendingOutputs.add(queue);
        }

        if (crafting) scheduler.schedule(craftCompletionTick, machine::finishCrafting);
        return machine;
    }

    public void assignInputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= recipe.inputCount()) throw new IllegalArgumentException("Invalid input slot " + slotIndex);
        inputFaces.put(face, slotIndex);
    }

    public void assignOutputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= recipe.outputCount()) throw new IllegalArgumentException("Invalid output slot " + slotIndex);
        outputFaces.put(face, slotIndex);
    }

    public void clearFaceAssignment(Direction face) {
        inputFaces.remove(face);
        outputFaces.remove(face);
    }

    public @Nullable Port inputPortForFace(Direction face) {
        Integer slot = inputFaces.get(face);
        return slot != null ? inputPort(slot) : null;
    }

    public Integer outputSlotForFace(Direction face) {
        return outputFaces.get(face);
    }

    public Map<Direction, Integer> getInputFaceAssignments() { return Map.copyOf(inputFaces); }
    public Map<Direction, Integer> getOutputFaceAssignments() { return Map.copyOf(outputFaces); }

    public Port inputPort(int index) {
        return new InputSlotPort(index);
    }
    public void setOutputPort(int index, Port port) {
        outputPorts.set(index, port);
    }

    public int inputSlotCount() { return recipe.inputCount(); }
    public int outputSlotCount() { return recipe.outputCount(); }

    public void tick(long currentTick) {
        tryFlushOutputs();
    }

    private void tryStartCrafting() {
        if (crafting) return;
        for (int i = 0; i < recipe.inputCount(); i++) {
            if (bufferedCounts[i] < recipe.inputs().get(i).amount()) return;
        }

        for (int i = 0; i < recipe.inputCount(); i++) {
            bufferedCounts[i] -= recipe.inputs().get(i).amount();
        }

        crafting = true;
        craftCompletionTick = scheduler.getCurrentTick() + recipe.durationTicks();
        scheduler.schedule(craftCompletionTick, this::finishCrafting);
    }

    private void finishCrafting() {
        crafting = false;
        for (int i = 0; i < recipe.outputCount(); i++) {
            RecipeIngredient outIngredient = recipe.outputs().get(i);
            Deque<Payload> queue = pendingOutputs.get(i);
            for (int n = 0; n < outIngredient.amount(); n++) queue.addLast(new Payload(outIngredient.itemId()));
        }
        tryFlushOutputs();
        tryStartCrafting();
    }

    private void tryFlushOutputs() {
        for (int i = 0; i < pendingOutputs.size(); i++) {
            Deque<Payload> queue = pendingOutputs.get(i);
            Port port = outputPorts.get(i);
            while (!queue.isEmpty() && port.canAccept(queue.peekFirst())) {
                port.accept(queue.pollFirst());
            }
        }
    }

    public MachineRecipe getRecipe() { return recipe; }
    public boolean isCrafting() { return crafting; }
    public long getCraftCompletionTick() { return craftCompletionTick; }
    public int[] getBufferedCounts() { return bufferedCounts.clone(); }
    public int getBufferMultiplier() { return bufferMultiplier; }

    public List<List<Identifier>> getPendingOutputItemIds() {
        List<List<Identifier>> result = new ArrayList<>();
        for (Deque<Payload> queue : pendingOutputs) {
            List<Identifier> typeIds = new ArrayList<>();
            for (Payload payload : queue) typeIds.add(payload.itemId());
            result.add(typeIds);
        }
        return result;
    }

    public boolean setRecipe(MachineRecipe newRecipe, List<Port> newOutputPorts) {
        if (crafting) return false;
        for (int count : bufferedCounts) if (count > 0) return false;
        for (Deque<Payload> queue : pendingOutputs) if (!queue.isEmpty()) return false;
        if (newOutputPorts.size() != newRecipe.outputCount()) {
            throw new IllegalArgumentException("Expected " + newRecipe.outputCount() + " output ports, got " + newOutputPorts.size());
        }

        this.recipe = newRecipe;
        this.bufferedCounts = new int[newRecipe.inputCount()];
        pendingOutputs.clear();
        outputPorts.clear();
        for (int i = 0; i < newRecipe.outputCount(); i++) pendingOutputs.add(new ArrayDeque<>());
        outputPorts.addAll(newOutputPorts);
        return true;
    }

    private final class InputSlotPort implements Port {
        private final int index;
        private InputSlotPort(int index) { this.index = index; }

        @Override
        public boolean canAccept(Payload payload) {
            RecipeIngredient ingredient = recipe.inputs().get(index);
            return payload.itemId().equals(ingredient.itemId()) && bufferedCounts[index] < ingredient.amount() * bufferMultiplier;
        }

        @Override
        public void accept(Payload payload) {
            if (!canAccept(payload)) throw new IllegalStateException("Machine input slot " + index + " cannot accept this payload right now");
            bufferedCounts[index]++;
            tryStartCrafting();
        }
    }
}