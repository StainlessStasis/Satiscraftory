package io.github.stainlessstasis.manifold.menu.machine;

import io.github.stainlessstasis.manifold.Scheduler;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Port;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.menu.MenuConstants;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldBlocks;
import io.github.stainlessstasis.manifold.registry.ManifoldMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MachineMenu extends AbstractContainerMenu {
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_DURATION = 1;
    public static final int DATA_FLAGS    = 2;
    public static final int DATA_SIZE     = 3;

    public static final int FLAG_CRAFTING = 1;
    public static final int FLAG_STALLED  = 2;

    private final Machine machine;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    private final int inputCount;
    private final int outputCount;

    // CLIENT STUFF
    /**
     * Clientside dummy constructor
     */
    public MachineMenu(
            int containerId, Inventory playerInventory, Machine dummyMachine,
            int[] inputX, int[] inputY,
            int[] outputX, int[] outputY,
            int playerInvX, int playerInvY
    ) {
        this(
                containerId, playerInventory, dummyMachine,
                inputX, inputY, outputX, outputY, playerInvX, playerInvY,
                ContainerLevelAccess.NULL,
                new SimpleContainerData(DATA_SIZE)
        );
    }

    public static MachineMenu fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        MachineRecipe recipe = readRecipe(buf);

        int inputCount = recipe.inputCount();
        int outputCount = recipe.outputCount();

        int[] inputX = new int[inputCount], inputY = new int[inputCount];
        for (int i = 0; i < inputCount; i++) { inputX[i] = buf.readVarInt(); inputY[i] = buf.readVarInt(); }

        int[] outputX = new int[outputCount], outputY = new int[outputCount];
        for (int i = 0; i < outputCount; i++) { outputX[i] = buf.readVarInt(); outputY[i] = buf.readVarInt(); }

        int playerInvX = buf.readVarInt();
        int playerInvY = buf.readVarInt();

        List<Port> dummyPorts = new ArrayList<>();
        for (int i = 0; i < outputCount; i++) dummyPorts.add(FactoryNetwork.NO_OP_PORT);

        Machine dummyMachine = new Machine(recipe, new Scheduler(), dummyPorts);

        return new MachineMenu(containerId, playerInventory, dummyMachine,
                inputX, inputY, outputX, outputY, playerInvX, playerInvY);
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
            int containerId, Inventory playerInventory, Machine machine,
            int[] inputX, int[] inputY,
            int[] outputX, int[] outputY,
            int playerInvX, int playerInvY,
            ContainerLevelAccess access, ContainerData serverData
    ) {
        super(ManifoldMenus.MACHINE.get(), containerId);
        this.machine = machine;
        this.access = access;
        this.inputCount = machine.inputSlotCount();
        this.outputCount = machine.outputSlotCount();

        checkContainerDataCount(serverData, DATA_SIZE);
        this.data = serverData;

        for (int i = 0; i < inputCount; i++) {
            addSlot(new MachineInputSlot(machine, i, inputX[i], inputY[i]));
        }

        for (int i = 0; i < outputCount; i++) {
            addSlot(new MachineOutputSlot(machine, i, outputX[i], outputY[i]));
        }

        // player inventory
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

        // hotbar
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
    public boolean stillValid(@NonNull Player player) {
        return AbstractContainerMenu.stillValid(access, player, ManifoldBlocks.MACHINE.get());
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        ItemStack quickMovedStack;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack rawStack = slot.getItem();
        quickMovedStack = rawStack.copy();

        int machineSlots = inputCount + outputCount;
        int playerInvStart = machineSlots;
        int playerInvEnd = machineSlots + MenuConstants.PLAYER_INV_SIZE;
        int hotbarStart = playerInvEnd;
        int hotbarEnd = hotbarStart + MenuConstants.HOTBAR_SIZE;

        if (index < machineSlots) {
            if (!moveItemStackTo(rawStack, playerInvStart, hotbarEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player inventory/hotbar -> try input slots
            if (!moveItemStackTo(rawStack, 0, inputCount, false)) {
                if (index < playerInvEnd) {
                    // Player inventory -> try hotbar
                    if (!moveItemStackTo(rawStack, hotbarStart, hotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Hotbar -> try player inventory
                    if (!moveItemStackTo(rawStack, playerInvStart, playerInvEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
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

    /**
     * Progress fraction in [0, 1] : 1 when stalled, 0 when idle
     */
    public float getProgressFraction() {
        if (isStalled()) return 1f;
        int duration = getDurationTicks();
        if (!isCrafting() || duration <= 0) return 0f;
        return (float) getCraftProgressTicks() / duration;
    }

    public Machine getMachine() { return machine; }
}