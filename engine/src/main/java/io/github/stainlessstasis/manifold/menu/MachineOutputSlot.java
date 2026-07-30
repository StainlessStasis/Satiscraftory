package io.github.stainlessstasis.manifold.menu;

import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class MachineOutputSlot extends MachineSlot {
    public MachineOutputSlot(Machine machine, int slotIndex, int x, int y) {
        super(machine, slotIndex, x, y);
    }

    @Override
    protected RecipeIngredient ingredient() {
        return machine.getRecipe().outputs().get(slotIndex);
    }

    @Override
    protected int getAmount() {
        return machine.getOutputAmount(slotIndex);
    }

    @Override
    protected int extract(int amount) {
        return machine.tryExtractOutput(slotIndex, amount);
    }

    @Override
    protected void setAmountClientSide(int amount) {
        machine.setOutputAmountClientSide(slotIndex, amount);
    }

    @Override
    protected int getCapacity() {
        return machine.getOutputCapacity(slotIndex);
    }

    @Override
    public @NonNull ItemStack safeInsert(@NonNull ItemStack stack, int amount) {
        if (!mayPlace(stack)) return stack;
        int toInsert = Math.min(amount, stack.getCount());
        int inserted = machine.tryInsertOutput(slotIndex, toInsert);
        if (inserted <= 0) return stack;
        ItemStack remainder = stack.copy();
        remainder.shrink(inserted);
        return remainder;
    }
}