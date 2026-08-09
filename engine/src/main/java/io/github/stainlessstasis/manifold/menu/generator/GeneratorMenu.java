package io.github.stainlessstasis.manifold.menu.generator;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory_component.generator.Generator;
import io.github.stainlessstasis.manifold.factory_component.generator.GeneratorBlockEntity;
import io.github.stainlessstasis.manifold.menu.ProgressBar;
import io.github.stainlessstasis.manifold.menu.SingleSlotMenu;
import io.github.stainlessstasis.manifold.registry.ManifoldMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import org.jspecify.annotations.NonNull;

public class GeneratorMenu extends SingleSlotMenu implements ProgressBar {
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 1;
    public static final int DATA_FLAGS    = 2;
    public static final int DATA_SIZE     = 3;
    public static final int FLAG_BURNING  = 1;

    private final Generator generator;
    private final double powerRateMw;

    /**
     * Clientside dummy constructor
     */
    public GeneratorMenu(
            int containerId, Inventory playerInventory, Generator dummyGenerator, double powerRateMw,
            int slotX, int slotY, int playerInvX, int playerInvY
    ) {
        this(
                containerId, playerInventory, dummyGenerator, powerRateMw,
                slotX, slotY, playerInvX, playerInvY,
                ContainerLevelAccess.NULL, new SimpleContainerData(DATA_SIZE)
        );
    }

    public static GeneratorMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        Identifier generatorType = buf.readIdentifier();
        double powerRateMw = buf.readDouble();

        int slotX = buf.readVarInt();
        int slotY = buf.readVarInt();
        int playerInvX = buf.readVarInt();
        int playerInvY = buf.readVarInt();

        Generator dummyGenerator = new Generator(generatorType, powerRateMw, new Scheduler());

        return new GeneratorMenu(containerId, playerInventory, dummyGenerator, powerRateMw,
                slotX, slotY, playerInvX, playerInvY);
    }

    public GeneratorMenu(
            int containerId, Inventory playerInventory, Generator generator, double powerRateMw,
            int slotX, int slotY, int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData serverData
    ) {
        super(
                ManifoldMenus.GENERATOR.get(), containerId, playerInventory,
                new GeneratorFuelSlot(generator, slotX, slotY),
                playerInvX, playerInvY, access, serverData, DATA_SIZE
        );
        this.generator = generator;
        this.powerRateMw = powerRateMw;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return super.stillValid(player) && access.evaluate(
                (level, pos) -> level.getBlockEntity(pos) instanceof GeneratorBlockEntity,
                true
        );
    }

    public int getBurnProgressTicks() {
        return data.get(DATA_PROGRESS);
    }

    public int getBurnDurationTicks() {
        return data.get(DATA_DURATION);
    }

    public boolean isBurning() {
        return (data.get(DATA_FLAGS) & FLAG_BURNING) != 0;
    }

    @Override
    public float getProgressFraction() {
        int duration = getBurnDurationTicks();
        if (!isBurning() || duration <= 0) return 0f;
        return (float) getBurnProgressTicks() / duration;
    }

    public double getCurrentPowerOutputMw() {
        return isBurning() ? powerRateMw : 0d;
    }

    public double getRatedPowerOutputMw() {
        return powerRateMw;
    }

    public Generator getGenerator() {
        return generator;
    }
}