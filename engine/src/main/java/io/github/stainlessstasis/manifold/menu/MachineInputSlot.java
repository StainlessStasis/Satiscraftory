package io.github.stainlessstasis.manifold.menu;

import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;

public class MachineInputSlot extends MachineSlot {
    public MachineInputSlot(Machine machine, int slotIndex, int x, int y) {
        super(machine, slotIndex, x, y);
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