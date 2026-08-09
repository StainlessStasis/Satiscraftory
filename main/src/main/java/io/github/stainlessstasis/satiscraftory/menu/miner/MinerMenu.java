package io.github.stainlessstasis.satiscraftory.menu.miner;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.menu.ProgressBar;
import io.github.stainlessstasis.manifold.menu.SingleSlotMenu;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jspecify.annotations.NonNull;

public class MinerMenu extends SingleSlotMenu implements ProgressBar {
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 1;
    public static final int DATA_FLAGS    = 2;
    public static final int DATA_SIZE     = 3;
    public static final int FLAG_ACTIVE   = 1;
    public static final int FLAG_POWERED  = 2;

    private final Producer producer;
    private final double powerDemandMw;

    /**
     * Clientside dummy constructor
     */
    public MinerMenu(
            int containerId, Inventory playerInventory, Producer dummyProducer, double powerDemandMw,
            int slotX, int slotY, int playerInvX, int playerInvY
    ) {
        this(
                containerId, playerInventory, dummyProducer, powerDemandMw,
                slotX, slotY, playerInvX, playerInvY,
                ContainerLevelAccess.NULL, new SimpleContainerData(DATA_SIZE)
        );
    }

    public static MinerMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        Identifier itemId = buf.readIdentifier();
        long intervalTicks = buf.readVarLong();
        double powerDemandMw = buf.readDouble();

        int slotX = buf.readVarInt();
        int slotY = buf.readVarInt();
        int playerInvX = buf.readVarInt();
        int playerInvY = buf.readVarInt();

        Producer dummyProducer = Producer.restore(
                itemId, intervalTicks, FactoryNetwork.NO_OP_PORT,
                new Scheduler(), false, 0, 0, false, -1
        );

        return new MinerMenu(containerId, playerInventory, dummyProducer, powerDemandMw,
                slotX, slotY, playerInvX, playerInvY);
    }

    public MinerMenu(
            int containerId, Inventory playerInventory, Producer producer, double powerDemandMw,
            int slotX, int slotY, int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData serverData
    ) {
        super(
                SCMenus.MINER.get(), containerId, playerInventory,
                new MinerOutputSlot(producer, slotX, slotY),
                playerInvX, playerInvY, access, serverData, DATA_SIZE
        );
        this.producer = producer;
        this.powerDemandMw = powerDemandMw;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return super.stillValid(player) && access.evaluate(
                (level, pos) -> level.getBlockEntity(pos) instanceof MinerBlockEntity,
                true
        );
    }

    public int getProductionProgressTicks() {
        return data.get(DATA_PROGRESS);
    }
    public int getIntervalTicks() {
        return data.get(DATA_DURATION);
    }
    public boolean isActive() {
        return (data.get(DATA_FLAGS) & FLAG_ACTIVE) != 0;
    }
    public boolean isPowered() {
        return (data.get(DATA_FLAGS) & FLAG_POWERED) != 0;
    }

    @Override
    public float getProgressFraction() {
        int duration = getIntervalTicks();
        if (!isActive() || duration <= 0) return 0f;
        return (float) getProductionProgressTicks() / duration;
    }

    public double getCurrentPowerConsumptionMw() {
        return isPowered() ? powerDemandMw : 0d;
    }
    public double getRatedPowerConsumptionMw() {
        return powerDemandMw;
    }

    public Producer getProducer() {
        return producer;
    }
}