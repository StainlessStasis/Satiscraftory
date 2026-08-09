package io.github.stainlessstasis.manifold.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.NonNull;

public abstract class SingleSlotMenu extends AbstractFactoryMenu {
    protected final ContainerLevelAccess access;
    protected final ContainerData data;

    protected SingleSlotMenu(
            MenuType<?> type, int containerId, Inventory playerInventory,
            Slot factorySlot, int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData data, int dataSize
    ) {
        super(type, containerId, 1);
        this.access = access;
        checkContainerDataCount(data, dataSize);
        this.data = data;

        addSlot(factorySlot);
        addPlayerInventorySlots(playerInventory, playerInvX, playerInvY);
        addDataSlots(data);
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return access.evaluate(
                (_, pos) -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64,
                true
        );
    }
}