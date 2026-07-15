package io.github.stainlessstasis.manifold.menu;

import io.github.stainlessstasis.manifold.registry.ManifoldMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ContainerMenu extends AbstractContainerMenu {
    public static final int ROWS = 3;
    public static final int SLOT_COUNT = ROWS * 9;

    private final Container container;

    public ContainerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT));
    }

    public ContainerMenu(int containerId, Inventory playerInventory, Container container) {
        super(ManifoldMenus.CONTAINER.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int playerInvY = 18 + ROWS * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 3 * 18 + 4));
        }
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            copy = stackInSlot.copy();
            if (index < SLOT_COUNT) {
                if (!moveItemStackTo(stackInSlot, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stackInSlot, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(@NonNull Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}