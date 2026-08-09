package io.github.stainlessstasis.manifold.menu.machine;

import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class MachineInputSlot extends MachineSlot {
    public MachineInputSlot(Machine machine, int slotIndex, int x, int y) {
        super(machine, slotIndex, x, y);
    }

    @Override
    public @NonNull ItemStack safeInsert(@NonNull ItemStack stack, int amount) {
        if (!mayPlace(stack)) return stack;

        int toInsert = Math.min(amount, stack.getCount());
        int room = getCapacity() - getAmount();
        int actualInsert = Math.min(toInsert, room);
        if (actualInsert <= 0) return stack;

        machine.inputPort(slotIndex).accept(new Payload(ingredient().itemId(), actualInsert));

        ItemStack remainder = stack.copy();
        remainder.shrink(actualInsert);
        return remainder;
    }

    @Override
    protected RecipeIngredient ingredient() {
        return machine.getRecipe().inputs().get(slotIndex);
    }

    @Override
    protected int getAmount() {
        return machine.getInputAmount(slotIndex);
    }

    @Override
    protected int extract(int amount) {
        return machine.tryExtractInput(slotIndex, amount);
    }

    @Override
    protected void setAmountClientSide(int amount) {
        machine.setInputAmountClientSide(slotIndex, amount);
    }

    @Override
    protected int getCapacity() {
        return machine.getInputCapacity(slotIndex);
    }
}