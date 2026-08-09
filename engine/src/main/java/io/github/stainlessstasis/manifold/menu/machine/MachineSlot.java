package io.github.stainlessstasis.manifold.menu.machine;

import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class MachineSlot extends Slot {
    protected final Machine machine;
    protected final int slotIndex;

    protected MachineSlot(Machine machine, int slotIndex, int x, int y) {
        super(new SimpleContainer(1), slotIndex, x, y);
        this.machine = machine;
        this.slotIndex = slotIndex;
    }

    protected abstract RecipeIngredient ingredient();
    protected abstract int getAmount();
    protected abstract int extract(int amount);
    protected abstract void setAmountClientSide(int amount);
    protected abstract int getCapacity();

    private Item resolvedItem() {
        return BuiltInRegistries.ITEM.getValue(ingredient().itemId());
    }

    @Override
    public @NonNull ItemStack getItem() {
        int count = getAmount();
        if (count <= 0) return ItemStack.EMPTY;
        return new ItemStack(resolvedItem(), count);
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        Identifier slotItemId = ingredient().itemId();
        Identifier stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return slotItemId.equals(stackItemId);
    }

    @Override
    public @NonNull ItemStack remove(int amount) {
        int taken = extract(amount);
        if (taken <= 0) return ItemStack.EMPTY;
        return new ItemStack(resolvedItem(), taken);
    }

    @Override
    public void set(@NonNull ItemStack stack) {
        setAmountClientSide(stack.getCount());
    }

    @Override
    public void setChanged() {
        // Machine handles this on its own
    }

    @Override
    public int getMaxStackSize() {
        return getCapacity();
    }

    @Override
    public boolean hasItem() {
        return getAmount() > 0;
    }
}