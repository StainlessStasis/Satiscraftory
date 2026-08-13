package io.github.stainlessstasis.satiscraftory.building.demolition;

import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.multiblock.MultiblockDemolition;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerBlock;
import io.github.stainlessstasis.manifold.recipe.RecipeIngredient;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
import io.github.stainlessstasis.satiscraftory.building.BuildingCost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class BuildGunDemolition {
    private BuildGunDemolition() {}

    /**
     * @return true if something was demolished
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean tryDemolish(ServerPlayer player, Level level, BlockPos targetPos) {
        DemolitionTarget target = DemolitionResolver.resolve(level, targetPos);
        if (target == null) return false;

        if (target.cost() != null && !player.isCreative() && SatiscraftoryConfig.BUILDING_COSTS.getAsBoolean()) {
            refund(player, target.cost());
        }

        removeTarget(level, target);
        return true;
    }

    private static void refund(ServerPlayer player, BuildingCost cost) {
        for (RecipeIngredient ingredient : cost.inputs()) {
            giveOrDrop(player, ingredient);
        }
    }

    private static void giveOrDrop(ServerPlayer player, RecipeIngredient ingredient) {
        Item item = BuiltInRegistries.ITEM.getOptional(ingredient.itemId()).orElse(null);
        if (item == null) return;

        int remaining = ingredient.amount();
        int maxStackSize = item.getDefaultMaxStackSize();
        while (remaining > 0) {
            int take = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(item, take);

            boolean fullyAdded = player.getInventory().add(stack);
            if (!fullyAdded && !stack.isEmpty()) {
                player.drop(stack, false);
            }

            remaining -= take;
        }
    }

    private static void removeTarget(Level level, DemolitionTarget target) {
        BlockPos targetPos = target.primaryPos();
        BlockState state = level.getBlockState(targetPos);

        if (state.getBlock() instanceof MultiblockFillerBlock) {
            level.removeBlock(targetPos, false);
            return;
        }

        if (level.getBlockEntity(targetPos) instanceof MultiblockControllerAccess controller) {
            MultiblockDemolition.demolishFillers(level, controller.getMultiblockFillerPositions());
            level.removeBlock(targetPos, false);
            return;
        }

        level.removeBlock(targetPos, false);
    }
}