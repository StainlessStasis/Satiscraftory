package io.github.stainlessstasis.manifold.factory_component.merger;

import io.github.stainlessstasis.manifold.factory_component.FactoryComponent;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.Port;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class Merger implements FactoryComponent {
    public static final int MAX_INPUTS = 3;

    private final Payload[] buffer = new Payload[MAX_INPUTS];
    private final Map<Direction, Integer> inputFaces = new EnumMap<>(Direction.class);
    private @Nullable Port output;
    /** The index of which input the merger is accepting from. Mergers merge payloads as round-robin */
    private int nextInputIndex = 0;

    public static Merger restore(int nextInputIndex, Map<Direction, Integer> inputFaces, Identifier[] bufferedItemIds) {
        Merger merger = new Merger();
        merger.nextInputIndex = nextInputIndex;
        for (var entry : inputFaces.entrySet()) merger.assignInputFace(entry.getKey(), entry.getValue());
        for (int i = 0; i < MAX_INPUTS && i < bufferedItemIds.length; i++) {
            if (bufferedItemIds[i] != null) merger.buffer[i] = new Payload(bufferedItemIds[i]);
        }
        return merger;
    }

    public void assignInputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_INPUTS) throw new IllegalArgumentException("Invalid input slot " + slotIndex);
        inputFaces.put(face, slotIndex);
    }

    public void clearFaceAssignment(Direction face) {
        inputFaces.remove(face);
    }

    public @Nullable Port inputPortForFace(Direction face) {
        Integer slot = inputFaces.get(face);
        return slot != null ? getInputPort(slot) : null;
    }

    public Map<Direction, Integer> getInputFaceAssignments() { return Map.copyOf(inputFaces); }

    public void setOutput(@Nullable Port output) { this.output = output; }
    public @Nullable Port getOutput() { return output; }
    public int getNextInputIndex() { return nextInputIndex; }

    @Override
    public void setOutputPort(int slot, Port port) { setOutput(port); }

    @Override
    public int outputSlotCount() { return 1; }

    public Identifier[] getBufferedItemIds() {
        Identifier[] result = new Identifier[MAX_INPUTS];
        for (int i = 0; i < MAX_INPUTS; i++) result[i] = (buffer[i] != null) ? buffer[i].itemId() : null;
        return result;
    }

    public Port getInputPort(int index) {
        return new Port() {
            @Override
            public boolean canAccept(Payload payload) { return buffer[index] == null; }

            @Override
            public void accept(Payload payload) { acceptWithOverflow(payload, 0); }

            @Override
            public void acceptWithOverflow(Payload payload, double overflowAmount) {
                if (buffer[index] != null) throw new IllegalStateException("Merger input " + index + " is jammed at entry");
                buffer[index] = payload;
            }
        };
    }

    public void tick(long currentTick) {
        for (int i = 0; i < MAX_INPUTS; i++) {
            int index = (nextInputIndex + i) % MAX_INPUTS;
            Payload payload = buffer[index];
            if (payload == null) continue;
            if (output != null && output.canAccept(payload)) {
                output.accept(payload);
                buffer[index] = null;
                nextInputIndex = (index + 1) % MAX_INPUTS;
                return;
            }
        }
    }
}