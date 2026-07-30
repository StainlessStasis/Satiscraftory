package io.github.stainlessstasis.manifold.factory_component.machine;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory_component.FactoryComponent;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class Machine implements FactoryComponent {
    private MachineRecipe recipe;
    private final Scheduler scheduler;
    private final int bufferMultiplier; // how many recipe batches worth of input/output each slot can hold at once
    private int[] inputCounts;
    private int[] outputCounts;

    private final List<Port> outputPorts = new ArrayList<>();
    private final Map<Direction, Integer> inputFaces = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer> outputFaces = new EnumMap<>(Direction.class);

    private boolean crafting = false;
    private boolean stalled = false; // craft finished, waiting for room in output slots
    private long craftCompletionTick = -1;
    private Scheduler.@Nullable ScheduledTask craftTask;

    private boolean powered = true;
    private long pausedRemainingTicks = -1; // -1 = not paused

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
        this.inputCounts = new int[recipe.inputCount()];
        this.outputCounts = new int[recipe.outputCount()];
        this.outputPorts.addAll(initialOutputPorts);
    }

    public static Machine restore(
            MachineRecipe recipe, Scheduler scheduler, List<Port> outputPorts,
            int bufferMultiplier, boolean crafting, boolean stalled, long craftCompletionTick,
            int[] inputCounts, int[] outputCounts,
            Map<Direction, Integer> inputFaces, Map<Direction, Integer> outputFaces
    ) {
        Machine machine = new Machine(recipe, scheduler, outputPorts, bufferMultiplier);
        machine.crafting = crafting;
        machine.stalled = stalled;
        machine.craftCompletionTick = craftCompletionTick;
        machine.inputCounts = inputCounts.clone();
        machine.outputCounts = outputCounts.clone();

        for (var entry : inputFaces.entrySet()) machine.assignInputFace(entry.getKey(), entry.getValue());
        for (var entry : outputFaces.entrySet()) machine.assignOutputFace(entry.getKey(), entry.getValue());

        if (crafting && !stalled) {
            machine.craftTask = scheduler.schedule(craftCompletionTick, machine::finishCrafting);
        }
        return machine;
    }

    public void assignInputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= recipe.inputCount()) {
            throw new IllegalArgumentException("Invalid input slot " + slotIndex);
        }
        inputFaces.put(face, slotIndex);
    }

    public void assignOutputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= recipe.outputCount()) {
            throw new IllegalArgumentException("Invalid output slot " + slotIndex);
        }
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

    @Override
    public void setOutputPort(int index, Port port) {
        outputPorts.set(index, port);
    }

    public int inputSlotCount() { return recipe.inputCount(); }

    @Override
    public int outputSlotCount() { return recipe.outputCount(); }

    public void tick(long currentTick) {
        tryFlushOutputs();
        if (stalled && tryDepositOutputs()) {
            stalled = false;
            crafting = false;
            tryFlushOutputs();
            tryStartCrafting();
        }
    }

    public void setPowered(boolean powered) {
        if (this.powered == powered) return;
        this.powered = powered;

        if (!powered) {
            pauseCraft();
        } else {
            resumeCraft();
            tryStartCrafting(); // in case the machine was idle when power came back
        }
    }

    public boolean isPowered() {
        return powered;
    }

    private void pauseCraft() {
        if (craftTask == null) return;
        pausedRemainingTicks = craftCompletionTick - scheduler.getCurrentTick();
        craftTask.cancel();
        craftTask = null;
    }

    private void resumeCraft() {
        if (pausedRemainingTicks < 0) return;
        craftCompletionTick = scheduler.getCurrentTick() + pausedRemainingTicks;
        craftTask = scheduler.schedule(craftCompletionTick, this::finishCrafting);
        pausedRemainingTicks = -1;
    }

    private void tryStartCrafting() {
        if (crafting || stalled || !powered) return;

        // check inputs are satisfied
        for (int i = 0; i < recipe.inputCount(); i++) {
            if (inputCounts[i] < recipe.inputs().get(i).amount()) return;
        }

        // consume inputs
        for (int i = 0; i < recipe.inputCount(); i++) {
            inputCounts[i] -= recipe.inputs().get(i).amount();
        }

        crafting = true;
        craftCompletionTick = scheduler.getCurrentTick() + recipe.durationTicks();
        craftTask = scheduler.schedule(craftCompletionTick, this::finishCrafting);
    }

    private void finishCrafting() {
        craftTask = null;
        if (!tryDepositOutputs()) {
            stalled = true;
            return;
        }
        crafting = false;
        tryFlushOutputs();
        tryStartCrafting();
    }

    private boolean tryDepositOutputs() {
        for (int i = 0; i < recipe.outputCount(); i++) {
            RecipeIngredient out = recipe.outputs().get(i);
            if (outputCounts[i] + out.amount() > getOutputCapacity(i)) return false;
        }
        for (int i = 0; i < recipe.outputCount(); i++) {
            outputCounts[i] += recipe.outputs().get(i).amount();
        }
        return true;
    }

    private void tryFlushOutputs() {
        boolean freedSpace = false;
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            Identifier itemId = recipe.outputs().get(i).itemId();
            while (outputCounts[i] > 0) {
                Payload single = new Payload(itemId, 1);
                if (!port.canAccept(single)) break;
                port.accept(single);
                outputCounts[i]--;
                freedSpace = true;
            }
        }
        if (freedSpace) tryStartCrafting();
    }

    public void cancelScheduledTask() {
        if (craftTask != null) {
            craftTask.cancel();
            craftTask = null;
        }
    }

    /**
     * Debug-only: abandon any in-progress craft and drain all buffers so setRecipe() can work.
     */
    public void forceClear() {
        if (craftTask != null) {
            craftTask.cancel();
            craftTask = null;
        }
        crafting = false;
        stalled = false;
        craftCompletionTick = -1;
        pausedRemainingTicks = -1;
        Arrays.fill(inputCounts, 0);
        Arrays.fill(outputCounts, 0);
    }

    public int getInputAmount(int index) {
        return inputCounts[index];
    }

    public int getInputCapacity(int index) {
        return recipe.inputs().get(index).amount() * bufferMultiplier;
    }

    public void setInputAmountClientSide(int index, int amount) {
        inputCounts[index] = amount;
    }

    public int tryExtractInput(int index, int amount) {
        int taken = Math.min(amount, inputCounts[index]);
        inputCounts[index] -= taken;
        return taken;
    }

    public int getOutputAmount(int index) {
        return outputCounts[index];
    }

    public int getOutputCapacity(int index) {
        return recipe.outputs().get(index).amount() * bufferMultiplier;
    }

    public int tryInsertOutput(int index, int amount) {
        int cap = getOutputCapacity(index);
        int room = cap - outputCounts[index];
        int inserted = Math.min(amount, room);
        outputCounts[index] += inserted;
        return inserted;
    }

    public int tryExtractOutput(int index, int amount) {
        int taken = Math.min(amount, outputCounts[index]);
        outputCounts[index] -= taken;
        return taken;
    }

    public void setOutputAmountClientSide(int index, int amount) {
        outputCounts[index] = amount;
    }

    public boolean setRecipe(MachineRecipe newRecipe, List<Port> newOutputPorts) {
        if (crafting || stalled) return false;
        for (int count : inputCounts) if (count > 0) return false;
        for (int count : outputCounts) if (count > 0) return false;
        if (newOutputPorts.size() != newRecipe.outputCount()) {
            throw new IllegalArgumentException("Expected " + newRecipe.outputCount() + " output ports, got " + newOutputPorts.size());
        }
        this.recipe = newRecipe;
        this.inputCounts = new int[newRecipe.inputCount()];
        this.outputCounts = new int[newRecipe.outputCount()];
        outputPorts.clear();
        outputPorts.addAll(newOutputPorts);
        return true;
    }

    public MachineRecipe getRecipe() { return recipe; }
    public boolean isCrafting() { return crafting; }
    public boolean isStalled() { return stalled; }
    public long getCraftCompletionTick() { return craftCompletionTick; }
    public int[] getInputCounts() { return inputCounts.clone(); }
    public int[] getOutputCounts() { return outputCounts.clone(); }
    public int getBufferMultiplier() { return bufferMultiplier; }
    public List<Port> getOutputPorts() { return List.copyOf(outputPorts); }

    private final class InputSlotPort implements Port {
        private final int index;
        private InputSlotPort(int index) { this.index = index; }

        @Override
        public boolean canAccept(Payload payload) {
            RecipeIngredient ingredient = recipe.inputs().get(index);
            return payload.itemId().equals(ingredient.itemId())
                    && inputCounts[index] < ingredient.amount() * bufferMultiplier;
        }

        @Override
        public void accept(Payload payload) {
            if (!canAccept(payload))
                throw new IllegalStateException("Machine input slot " + index + " cannot accept this payload right now");
            inputCounts[index] += payload.count();
            tryStartCrafting();
        }
    }
}