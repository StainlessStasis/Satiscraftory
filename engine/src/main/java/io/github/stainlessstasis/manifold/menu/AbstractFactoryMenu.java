package io.github.stainlessstasis.manifold.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class AbstractFactoryMenu extends AbstractContainerMenu {
    protected final int factorySlotCount;

    protected AbstractFactoryMenu(MenuType<?> type, int containerId, int factorySlotCount) {
        super(type, containerId);
        this.factorySlotCount = factorySlotCount;
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int playerInvX, int playerInvY) {
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
    }

    protected int quickInsertRangeEnd() {
        return factorySlotCount;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack rawStack = slot.getItem();
        ItemStack quickMovedStack = rawStack.copy();

        int playerInvStart = factorySlotCount;
        int playerInvEnd = playerInvStart + MenuConstants.PLAYER_INV_SIZE;
        int hotbarStart = playerInvEnd;
        int hotbarEnd = hotbarStart + MenuConstants.HOTBAR_SIZE;

        if (index < factorySlotCount) {
            // Factory slot -> player inventory
            int startAmount = rawStack.getCount();
            if (!moveItemStackTo(rawStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
            int extracted = startAmount - rawStack.getCount();
            if (extracted > 0) slot.remove(extracted);
        } else if (!tryQuickInsertIntoFactorySlots(rawStack)) {
            if (index < playerInvEnd) {
                if (!moveItemStackTo(rawStack, hotbarStart, hotbarEnd, false)) return ItemStack.EMPTY;
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

    private boolean tryQuickInsertIntoFactorySlots(ItemStack rawStack) {
        boolean movedAny = false;
        for (int i = 0; i < quickInsertRangeEnd() && !rawStack.isEmpty(); i++) {
            Slot slot = this.slots.get(i);
            if (!slot.mayPlace(rawStack)) continue;
            int before = rawStack.getCount();
            ItemStack remainder = slot.safeInsert(rawStack, rawStack.getCount());
            rawStack.setCount(remainder.getCount());
            slot.setChanged();
            if (rawStack.getCount() != before) movedAny = true;
        }
        return movedAny;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }
}