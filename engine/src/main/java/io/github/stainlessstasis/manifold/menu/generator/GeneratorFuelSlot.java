package io.github.stainlessstasis.manifold.menu.generator;

import io.github.stainlessstasis.manifold.factory_component.generator.Generator;
import io.github.stainlessstasis.manifold.recipe.ManifoldGeneratorFuels;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class GeneratorFuelSlot extends Slot {
    private final Generator generator;

    public GeneratorFuelSlot(Generator generator, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        this.generator = generator;
    }

    @Override
    public @NonNull ItemStack getItem() {
        Identifier itemId = generator.getHeldItemId();
        int count = generator.getHeldCount();
        if (itemId == null || count <= 0) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), count);
    }

    @Override
    public boolean mayPlace(@NonNull ItemStack stack) {
        Identifier stackItemId = ItemUtils.idOf(stack.getItem());
        return ManifoldGeneratorFuels.isValidFuel(generator.getGeneratorType(), stackItemId);
    }

    @Override
    public @NonNull ItemStack remove(int amount) {
        Identifier itemId = generator.getHeldItemId();
        int taken = generator.tryExtractHeld(amount);
        if (taken <= 0 || itemId == null) return ItemStack.EMPTY;
        return new ItemStack(BuiltInRegistries.ITEM.getValue(itemId), taken);
    }

    @Override
    public void set(@NonNull ItemStack stack) {
        Identifier itemId = stack.isEmpty() ? null : ItemUtils.idOf(stack.getItem());
        generator.setHeldClientSide(itemId, stack.getCount());
    }

    @Override
    public void setChanged() {}

    @Override
    public int getMaxStackSize() {
        return generator.getCapacity();
    }

    @Override
    public boolean hasItem() {
        return generator.getHeldCount() > 0;
    }
}