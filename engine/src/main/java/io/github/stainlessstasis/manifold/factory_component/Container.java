package io.github.stainlessstasis.manifold.factory_component;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Container implements Port {
    private final Payload[] slots;
    private @Nullable Port output;
    private int nextExtractIndex = 0;

    public Container(int slotCount) {
        this.slots = new Payload[slotCount];
    }

    public int getSlotCount() {
        return slots.length;
    }

    public @Nullable Payload getSlot(int index) {
        return slots[index];
    }

    public void setSlot(int index, @Nullable Payload payload) {
        slots[index] = payload;
    }

    public boolean isEmpty() {
        for (Payload slot : slots) if (slot != null) return false;
        return true;
    }

    public void clear() {
        Arrays.fill(slots, null);
    }

    public void setOutput(@Nullable Port output) {
        this.output = output;
    }

    @Override
    public boolean canAccept(Payload payload) {
        return findSlotFor(payload) >= 0;
    }

    @Override
    public void accept(Payload payload) {
        int index = findSlotFor(payload);
        if (index < 0) throw new IllegalStateException("Container has no room for this payload");
        Payload existing = slots[index];
        slots[index] = existing == null ? payload : existing.withCount(existing.count() + payload.count());
    }

    /** A matching partial stack with room for this payload, or the first empty slot; -1 if neither exists*/
    private int findSlotFor(Payload payload) {
        int maxStack = maxStackSizeFor(payload.itemId());
        int firstEmpty = -1;
        for (int i = 0; i < slots.length; i++) {
            Payload slot = slots[i];
            if (slot == null) {
                if (firstEmpty < 0) firstEmpty = i;
                continue;
            }
            if (!slot.hasExtraData() && !payload.hasExtraData()
                    && slot.itemId().equals(payload.itemId())
                    && slot.count() + payload.count() <= maxStack) {
                return i;
            }
        }
        return firstEmpty;
    }

    private static int maxStackSizeFor(Identifier itemId) {
        return BuiltInRegistries.ITEM.getOptional(itemId).map(Item::getDefaultMaxStackSize).orElse(64);
    }

    public void tick(long currentTick) {
        if (output == null) return;

        for (int attempt = 0; attempt < slots.length; attempt++) {
            int i = (nextExtractIndex + attempt) % slots.length;
            Payload slot = slots[i];
            if (slot == null) continue;

            Payload single = slot.withCount(1);
            if (!output.canAccept(single)) continue;

            output.accept(single);
            slots[i] = slot.count() > 1 ? slot.withCount(slot.count() - 1) : null;
            nextExtractIndex = (i + 1) % slots.length;
            break;
        }
    }

    public List<Payload> snapshotSlots() {
        List<Payload> result = new ArrayList<>(slots.length);
        result.addAll(Arrays.asList(slots));
        return result;
    }

    public static Container restore(int slotCount, List<Payload> savedSlots) {
        Container container = new Container(slotCount);
        for (int i = 0; i < slotCount && i < savedSlots.size(); i++) {
            container.slots[i] = savedSlots.get(i);
        }
        return container;
    }
}