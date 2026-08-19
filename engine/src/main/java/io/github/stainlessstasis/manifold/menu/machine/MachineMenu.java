package io.github.stainlessstasis.manifold.menu.machine;

import io.github.stainlessstasis.manifold.util.Scheduler;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.menu.AbstractFactoryMenu;
import io.github.stainlessstasis.manifold.menu.ProgressBar;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import io.github.stainlessstasis.manifold.registry.ManifoldMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MachineMenu extends AbstractFactoryMenu implements ProgressBar {
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 1;
    public static final int DATA_FLAGS    = 2;
    public static final int DATA_SIZE     = 3;

    public static final int FLAG_CRAFTING = 1;
    public static final int FLAG_STALLED  = 2;
    public static final int FLAG_POWERED  = 4;

    private final Machine machine;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final int inputCount;
    private final int outputCount;
    private final double powerDemandMw;
    private final BlockPos blockPos;

    /**
     * Clientside dummy constructor
     */
    public MachineMenu(
            int containerId, Inventory playerInventory, Machine dummyMachine, double powerDemandMw,
            int[] inputX, int[] inputY, int[] outputX, int[] outputY,
            int playerInvX, int playerInvY, BlockPos blockPos
    ) {
        this(containerId, playerInventory, dummyMachine, powerDemandMw,
                inputX, inputY, outputX, outputY, playerInvX, playerInvY,
                ContainerLevelAccess.NULL, new SimpleContainerData(DATA_SIZE), blockPos);
    }

    public static MachineMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        MachineRecipe recipe = readRecipe(buf);

        int inputCount = recipe.inputCount();
        int outputCount = recipe.outputCount();

        int[] inputX = new int[inputCount], inputY = new int[inputCount];
        for (int i = 0; i < inputCount; i++) { inputX[i] = buf.readVarInt(); inputY[i] = buf.readVarInt(); }

        int[] outputX = new int[outputCount], outputY = new int[outputCount];
        for (int i = 0; i < outputCount; i++) { outputX[i] = buf.readVarInt(); outputY[i] = buf.readVarInt(); }

        double powerDemandMw = buf.readDouble();

        int playerInvX = buf.readVarInt();
        int playerInvY = buf.readVarInt();

        List<Port> dummyPorts = new ArrayList<>();
        for (int i = 0; i < outputCount; i++) dummyPorts.add(FactoryNetwork.NO_OP_PORT);

        Machine dummyMachine = new Machine(recipe, new Scheduler(), dummyPorts);

        return new MachineMenu(containerId, playerInventory, dummyMachine, powerDemandMw,
                inputX, inputY, outputX, outputY, playerInvX, playerInvY, pos);
    }

    private static MachineRecipe readRecipe(RegistryFriendlyByteBuf buf) {
        Identifier id = buf.readIdentifier();
        Identifier machineType = buf.readIdentifier();
        List<RecipeIngredient> inputs = readIngredients(buf);
        List<RecipeIngredient> outputs = readIngredients(buf);
        long duration = buf.readVarLong();
        return new MachineRecipe(id, machineType, inputs, outputs, duration);
    }

    private static List<RecipeIngredient> readIngredients(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<RecipeIngredient> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(new RecipeIngredient(buf.readIdentifier(), buf.readVarInt()));
        }
        return list;
    }

    // SERVER STUFF
    public MachineMenu(
            int containerId, Inventory playerInventory, Machine machine, double powerDemandMw,
            int[] inputX, int[] inputY, int[] outputX, int[] outputY,
            int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData serverData, BlockPos blockPos
    ) {
        super(ManifoldMenus.MACHINE.get(), containerId, machine.inputSlotCount() + machine.outputSlotCount());
        this.machine = machine;
        this.powerDemandMw = powerDemandMw;
        this.access = access;
        this.inputCount = machine.inputSlotCount();
        this.outputCount = machine.outputSlotCount();
        this.blockPos = blockPos;

        checkContainerDataCount(serverData, DATA_SIZE);
        this.data = serverData;

        for (int i = 0; i < inputCount; i++) {
            addSlot(new MachineInputSlot(machine, i, inputX[i], inputY[i]));
        }
        for (int i = 0; i < outputCount; i++) {
            addSlot(new MachineOutputSlot(machine, i, outputX[i], outputY[i]));
        }

        addPlayerInventorySlots(playerInventory, playerInvX, playerInvY);
        addDataSlots(data);
    }

    @Override
    protected int quickInsertRangeEnd() {
        return inputCount;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return AbstractContainerMenu.stillValid(access, player, ManifoldBlocks.MACHINE.get());
    }

    public int getCraftProgressTicks() {
        return data.get(DATA_PROGRESS);
    }
    public int getDurationTicks() {
        return data.get(DATA_DURATION);
    }
    public boolean isCrafting() {
        return (data.get(DATA_FLAGS) & FLAG_CRAFTING) != 0;
    }
    public boolean isStalled() {
        return (data.get(DATA_FLAGS) & FLAG_STALLED) != 0;
    }
    public boolean isPowered() {
        return (data.get(DATA_FLAGS) & FLAG_POWERED) != 0;
    }

    @Override
    public float getProgressFraction() {
        if (isStalled()) return 1f;
        int duration = getDurationTicks();
        if (!isCrafting() || duration <= 0) return 0f;
        return Math.min(1f, (float) getCraftProgressTicks() / duration);
    }

    public double getCurrentPowerConsumptionMw() {
        return isPowered() ? powerDemandMw : 0d;
    }
    public double getRatedPowerConsumptionMw() {
        return powerDemandMw;
    }

    public Machine getMachine() { return machine; }

    public BlockPos getBlockPos() { return blockPos; }
}