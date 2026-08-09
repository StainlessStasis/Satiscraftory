package io.github.stainlessstasis.manifold.menu;

import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class SingleSlotMenu extends AbstractContainerMenu {
    protected static final int FACTORY_SLOT_INDEX = 0;

    protected final ContainerLevelAccess access;
    protected final ContainerData data;

    protected SingleSlotMenu(
            MenuType<?> type, int containerId, Inventory playerInventory,
            Slot factorySlot, int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData data, int dataSize
    ) {
        super(type, containerId);
        this.access = access;
        checkContainerDataCount(data, dataSize);
        this.data = data;

        addSlot(factorySlot);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        playerInvX + col * 18,
                        playerInvY + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                    playerInventory,
                    col,
                    playerInvX + col * 18,
                    playerInvY + 58
            ));
        }

        addDataSlots(data);
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack rawStack = slot.getItem();
        ItemStack quickMovedStack = rawStack.copy();

        int playerInvStart = FACTORY_SLOT_INDEX + 1;
        int playerInvEnd = playerInvStart + MenuConstants.PLAYER_INV_SIZE;
        int hotbarStart = playerInvEnd;
        int hotbarEnd = hotbarStart + MenuConstants.HOTBAR_SIZE;

        if (index == FACTORY_SLOT_INDEX) {
            if (!moveItemStackTo(rawStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(rawStack, FACTORY_SLOT_INDEX, FACTORY_SLOT_INDEX + 1, false)) {
            if (index < playerInvEnd) {
                if (!moveItemStackTo(rawStack, hotbarStart, hotbarEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(rawStack, playerInvStart, playerInvEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (rawStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, rawStack);
        return quickMovedStack;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return access.evaluate(
                (_, pos) -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64,
                true
        );
    }
}