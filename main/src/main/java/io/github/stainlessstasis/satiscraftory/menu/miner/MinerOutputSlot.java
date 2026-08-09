package io.github.stainlessstasis.satiscraftory.menu.miner;

import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class MinerOutputSlot extends Slot {
    private final Producer producer;

    public MinerOutputSlot(Producer producer, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        this.producer = producer;
    }

    @Override
    public @NonNull ItemStack getItem() {
        int count = producer.getBufferedCount();
        if (count <= 0) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.getValue(producer.getItemId()), count);
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        Identifier stackItemId = ItemUtils.idOf(stack.getItem());
        return stackItemId.equals(producer.getItemId());
    }

    @Override
    public @NonNull ItemStack remove(int amount) {
        Identifier itemId = producer.getItemId();
        int taken = producer.tryExtractBuffered(amount);
        if (taken <= 0) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), taken);
    }

    @Override
    public void set(@NonNull ItemStack stack) {
        producer.setBufferedCountClientSide(stack.getCount());
    }

    @Override
    public void setChanged() {}

    @Override
    public int getMaxStackSize() {
        return producer.getBufferCapacity();
    }

    @Override
    public boolean hasItem() {
        return producer.getBufferedCount() > 0;
    }

    @Override
    public @NonNull ItemStack safeInsert(@NonNull ItemStack stack, int amount) {
        if (!mayPlace(stack)) return stack;
        int toInsert = Math.min(amount, stack.getCount());
        int inserted = producer.tryInsertBuffered(toInsert);
        if (inserted <= 0) return stack;
        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }
}