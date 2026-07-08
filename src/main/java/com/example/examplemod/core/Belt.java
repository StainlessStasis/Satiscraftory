package com.example.examplemod.core;

import java.util.ArrayList;
import java.util.List;


public class Belt implements Port {
    private static class BeltItem {
        final Payload payload;
        double position; // 0 (just entered) to 1 (at exit)

        BeltItem(Payload payload, double position) {
            this.payload = payload;
            this.position = position;
        }
    }

    private final double speed;
    private final double minGap; // minimum gap between belt items
    private Port output;
    private final List<BeltItem> items = new ArrayList<>();

    public Belt(int lengthInTicks, double minGap) {
        if (lengthInTicks <= 0) throw new IllegalArgumentException("Length must be positive");
        this.speed = 1d / lengthInTicks;
        this.minGap = minGap;
    }

    public void setOutput(Port output) {
        this.output = output;
    }

    @Override
    public boolean canAccept(Payload payload) {
        if (items.isEmpty()) return true;
        BeltItem back = items.getLast();
        return back.position >= minGap;
    }

    @Override
    public void accept(Payload payload) {
        if (!canAccept(payload)) throw new IllegalStateException("Belt is jammed at entry");
        items.add(new BeltItem(payload, 0));
    }

    public void tick(long currentTick) {
        for (int i = 0; i < items.size(); i++) {
            BeltItem item = items.get(i);
            double cap = (i == 0) ? 1 : items.get(i - 1).position - minGap;
            double proposed = item.position + speed;
            item.position = Math.min(proposed, cap);
            if (item.position < 0) item.position = 0;
        }

        // try to discharge the front payload if it has reached the exit
        if (!items.isEmpty()) {
            BeltItem front = items.getFirst();
            if (front.position >= 1) {
                if (output != null && output.canAccept(front.payload)) {
                    output.accept(front.payload);
                    items.removeFirst();
                }
            }
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public List<Double> getPositions() {
        List<Double> positions = new ArrayList<>();
        for (BeltItem item : items) positions.add(item.position);
        return positions;
    }
}
