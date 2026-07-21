package io.github.stainlessstasis.manifold.factory_component;

import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Splitter {
    public static final int MAX_OUTPUTS = 3;

    private final List<Port> outputs = new ArrayList<>(MAX_OUTPUTS);
    private final Map<Direction, Integer> outputFaces = new EnumMap<>(Direction.class);
    /** The index of which output the splitter is outputting to. Splitters distribute payloads as round-robin */
    private int nextOutputIndex = 0;

    public Splitter() {
        for (int i = 0; i < MAX_OUTPUTS; i++) outputs.add(null);
    }

    public static Splitter restore(int nextOutputIndex, Map<Direction, Integer> outputFaces) {
        Splitter splitter = new Splitter();
        splitter.nextOutputIndex = nextOutputIndex;
        for (var entry : outputFaces.entrySet()) splitter.assignOutputFace(entry.getKey(), entry.getValue());
        return splitter;
    }

    public void assignOutputFace(Direction face, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= MAX_OUTPUTS) throw new IllegalArgumentException("Invalid output slot " + slotIndex);
        outputFaces.put(face, slotIndex);
    }

    public void clearFaceAssignment(Direction face) {
        outputFaces.remove(face);
    }

    public Integer outputSlotForFace(Direction face) {
        return outputFaces.get(face);
    }

    public Map<Direction, Integer> getOutputFaceAssignments() { return Map.copyOf(outputFaces); }

    public void setOutput(int index, @Nullable Port port) { outputs.set(index, port); }
    public @Nullable Port getOutput(int index) { return outputs.get(index); }
    public int getNextOutputIndex() { return nextOutputIndex; }

    private int nextConnectedFrom(int start) {
        for (int i = 0; i < MAX_OUTPUTS; i++) {
            int index = (start + i) % MAX_OUTPUTS;
            if (outputs.get(index) != null) return index;
        }
        return -1;
    }

    public final Port inputPort = new Port() {
        @Override
        public boolean canAccept(Payload payload) {
            int index = nextConnectedFrom(nextOutputIndex);
            return index >= 0 && outputs.get(index).canAccept(payload);
        }

        @Override
        public void accept(Payload payload) { acceptWithOverflow(payload, 0); }

        @Override
        public void acceptWithOverflow(Payload payload, double overflowAmount) {
            int index = nextConnectedFrom(nextOutputIndex);
            if (index < 0 || !outputs.get(index).canAccept(payload)) {
                throw new IllegalStateException("Splitter is jammed at entry");
            }
            outputs.get(index).acceptWithOverflow(payload, overflowAmount);
            nextOutputIndex = (index + 1) % MAX_OUTPUTS;
        }
    };
}