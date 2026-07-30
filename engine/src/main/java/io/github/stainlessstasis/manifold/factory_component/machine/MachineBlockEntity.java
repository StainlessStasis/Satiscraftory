package io.github.stainlessstasis.manifold.factory_component.machine;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.menu.MachineContainerData;
import io.github.stainlessstasis.manifold.menu.MachineMenu;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.extensions.IMenuProviderExtension;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MachineBlockEntity extends BlockEntity implements MenuProvider, IMenuProviderExtension {
    private static final Identifier DEFAULT_RECIPE_ID = Manifold.id("basic_processing");
    private Identifier pendingRecipeId; // for the presetrecipe command
    private static final double DEMAND_MW = 10d;

    private static final int[] INPUT_X = {21};
    private static final int[] INPUT_Y = {26};
    private static final int[] OUTPUT_X = {139};
    private static final int[] OUTPUT_Y = {26};
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;

    private Machine machine;

    public MachineBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.MACHINE.get(), pos, state);
    }

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        GlobalPos globalPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());

        machine = network.getOrCreateMachine(globalPos, () -> {
            MachineRecipe recipe = ManifoldRecipes.get(DEFAULT_RECIPE_ID);
            if (recipe == null) {
                throw new IllegalStateException("Missing built-in recipe " + DEFAULT_RECIPE_ID + " - check Manifold's own datapack resources");
            }
            return new Machine(recipe, network.getScheduler(), List.of(FactoryNetwork.NO_OP_PORT));
        });
        network.getPowerGrid().registerConsumer(globalPos, DEMAND_MW, machine::setPowered);

        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        machine.assignOutputFace(facing, 0);
        machine.assignInputFace(facing.getOpposite(), 0);

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());

        tryApplyPendingRecipe();
    }

    public void setPendingRecipe(Identifier recipeId) {
        this.pendingRecipeId = recipeId;
        tryApplyPendingRecipe();
    }

    private void tryApplyPendingRecipe() {
        if (pendingRecipeId == null || machine == null) return;

        Identifier recipeId = pendingRecipeId;
        pendingRecipeId = null;

        MachineRecipe recipe = ManifoldRecipes.get(recipeId);
        if (recipe == null) return;

        machine.setRecipe(recipe, machine.getOutputPorts());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos selfPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        Direction outputDirection = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos outputPos = getBlockPos().relative(outputDirection);
        network.linkMachineOutput(selfPos, 0, GlobalPos.of(serverLevel.dimension(), outputPos), outputDirection);
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) {
            relink(FactoryNetwork.get(serverLevel));
        }
    }

    private BlockPos resolveOutputPos() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return getBlockPos().relative(facing);
    }

    @Override
    public void writeClientSideData(@NonNull AbstractContainerMenu menu, @NonNull RegistryFriendlyByteBuf buf) {
        writeRecipe(buf, machine.getRecipe());
        for (int i = 0; i < INPUT_X.length; i++) {
            buf.writeVarInt(INPUT_X[i]); buf.writeVarInt(INPUT_Y[i]);
        }
        for (int i = 0; i < OUTPUT_X.length; i++) {
            buf.writeVarInt(OUTPUT_X[i]); buf.writeVarInt(OUTPUT_Y[i]);
        }
        buf.writeVarInt(PLAYER_INV_X);
        buf.writeVarInt(PLAYER_INV_Y);
    }

    private static void writeRecipe(RegistryFriendlyByteBuf buf, MachineRecipe recipe) {
        buf.writeIdentifier(recipe.id());
        buf.writeIdentifier(recipe.machineType());
        writeIngredients(buf, recipe.inputs());
        writeIngredients(buf, recipe.outputs());
        buf.writeVarLong(recipe.durationTicks());
    }

    private static void writeIngredients(RegistryFriendlyByteBuf buf, List<RecipeIngredient> ingredients) {
        buf.writeVarInt(ingredients.size());
        for (RecipeIngredient ingredient : ingredients) {
            buf.writeIdentifier(ingredient.itemId());
            buf.writeVarInt(ingredient.amount());
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NonNull Inventory playerInventory, @NonNull Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        return new MachineMenu(
                containerId, playerInventory, machine,
                INPUT_X, INPUT_Y, OUTPUT_X, OUTPUT_Y, PLAYER_INV_X, PLAYER_INV_Y,
                ContainerLevelAccess.create(serverLevel, getBlockPos()),
                new MachineContainerData(machine, serverLevel::getGameTime)
        );
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable("block.manifold.machine");
    }

    public Machine getMachine() { return machine; }
}