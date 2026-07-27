package io.github.stainlessstasis.manifold.command;

import io.github.stainlessstasis.manifold.factory_component.machine.MachineBlockEntity;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.machine.Machine;
import io.github.stainlessstasis.manifold.recipe.MachineRecipe;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

public final class FactoryCommands {
    private static final Predicate<CommandSourceStack> GAMEMASTER =
            source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    private static final double DEBUG_REACH = 5.0;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("factory")
                .then(Commands.literal("count")
                        .then(Commands.literal("belts")
                                .executes(ctx -> reportCount(ctx, "Belts", FactoryNetwork::getBeltCount)))
                        .then(Commands.literal("producers")
                                .executes(ctx -> reportCount(ctx, "Producers", FactoryNetwork::getProducerCount)))
                        .then(Commands.literal("consumers")
                                .executes(ctx -> reportCount(ctx, "Consumers", FactoryNetwork::getConsumerCount)))
                        .then(Commands.literal("machines")
                                .executes(ctx -> reportCount(ctx, "Machines", FactoryNetwork::getMachineCount)))
                        .then(Commands.literal("containers")
                                .executes(ctx -> reportCount(ctx, "Containers", FactoryNetwork::getContainerCount)))
                        .executes(FactoryCommands::reportAll)
                )

                .then(Commands.literal("loaded")
                        .requires(GAMEMASTER)
                        .then(Commands.literal("belts")
                                .executes(ctx -> reportLoadedCount(ctx, "Belts", FactoryNetwork::getLoadedBeltCount)))
                        .then(Commands.literal("producers")
                                .executes(ctx -> reportLoadedCount(ctx, "Producers", FactoryNetwork::getLoadedProducerCount)))
                        .then(Commands.literal("consumers")
                                .executes(ctx -> reportLoadedCount(ctx, "Consumers", FactoryNetwork::getLoadedConsumerCount)))
                        .then(Commands.literal("machines")
                                .executes(ctx -> reportLoadedCount(ctx, "Machines", FactoryNetwork::getLoadedMachineCount)))
                        .then(Commands.literal("containers")
                                .executes(ctx -> reportLoadedCount(ctx, "Containers", FactoryNetwork::getLoadedContainerCount)))
                        .executes(FactoryCommands::reportAllLoaded)
                )

                .then(Commands.literal("freeze")
                        .requires(GAMEMASTER)
                        .executes(ctx -> setFrozen(ctx, true)))
                .then(Commands.literal("unfreeze")
                        .requires(GAMEMASTER)
                        .executes(ctx -> setFrozen(ctx, false)))

                .then(Commands.literal("setrecipe")
                        .requires(GAMEMASTER)
                        .then(Commands.argument("recipe", IdentifierArgument.id())
                                .suggests((_, builder) -> SharedSuggestionProvider.suggestResource(
                                        ManifoldRecipes.allRecipes().keySet(), builder))
                                .executes(ctx -> setRecipe(ctx, null, false))
                                .then(Commands.literal("force")
                                        .executes(ctx -> setRecipe(ctx, null, true)))
                                .then(Commands.literal("at")
                                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                .executes(ctx -> setRecipe(ctx, BlockPosArgument.getBlockPos(ctx, "pos"), false))
                                                .then(Commands.literal("force")
                                                        .executes(ctx -> setRecipe(ctx, BlockPosArgument.getBlockPos(ctx, "pos"), true)))))
                        )
                )
        );
    }

    private static int setRecipe(CommandContext<CommandSourceStack> ctx, BlockPos explicitPos, boolean force) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Identifier recipeId = IdentifierArgument.getId(ctx, "recipe");

        MachineRecipe recipe = ManifoldRecipes.get(recipeId);
        if (recipe == null) {
            source.sendFailure(Component.literal("No such recipe: " + recipeId));
            return 0;
        }

        BlockPos pos = explicitPos != null ? explicitPos : lookingAtBlock(source);
        if (pos == null) {
            source.sendFailure(Component.literal("No target block - stand and look at a machine, or use 'at <pos>'"));
            return 0;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MachineBlockEntity machineBE)) {
            source.sendFailure(Component.literal("Block at " + pos.toShortString() + " is not a machine"));
            return 0;
        }

        Machine machine = machineBE.getMachine();
        if (recipe.inputCount() != 1 || recipe.outputCount() != 1) {
            source.sendFailure(Component.literal(
                    "setrecipe currently only supports 1-input/1-output recipes " +
                            "(got " + recipe.inputCount() + " in / " + recipe.outputCount() + " out)"))
            ;
            return 0;
        }

        if (force) machine.forceClear();

        boolean ok = machine.setRecipe(recipe, machine.getOutputPorts());
        if (!ok) {
            source.sendFailure(Component.literal(
                    "Machine at " + pos.toShortString() + " is busy. Retry with 'force'"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Set recipe at " + pos.toShortString() + " to " + recipeId), true);
        return 1;
    }

    private static BlockPos lookingAtBlock(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return null;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1f);
        Vec3 target = eye.add(look.scale(DEBUG_REACH));
        BlockHitResult hit = player.level().clip(
                new ClipContext(eye, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player)
        );
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;
    }

    private static int reportCount(CommandContext<CommandSourceStack> ctx, String label, ToIntFunction<FactoryNetwork> counter) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork network = FactoryNetwork.get(level);
        int count = counter.applyAsInt(network);
        ctx.getSource().sendSuccess(() -> Component.literal(label + ": " + count), false);
        return count;
    }

    private static int reportAll(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork network = FactoryNetwork.get(level);
        int belts = network.getBeltCount();
        int producers = network.getProducerCount();
        int consumers = network.getConsumerCount();
        int machines = network.getMachineCount();
        int containers = network.getContainerCount();
        int total = belts + producers + consumers + machines + containers;

        ctx.getSource().sendSuccess(() -> Component.literal(
                ("""
                        
                        Factory components:
                        Belts: %d, Producers: %d, Consumers: %d, Machines: %d, Containers: %d
                        Total: %d""")
                        .formatted(belts, producers, consumers, machines, containers, total)), false);
        return total;
    }

    private static int reportLoadedCount(CommandContext<CommandSourceStack> ctx, String label, ToIntBiFunction<FactoryNetwork, MinecraftServer> counter) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork network = FactoryNetwork.get(level);
        int count = counter.applyAsInt(network, level.getServer());
        ctx.getSource().sendSuccess(() -> Component.literal(label + " (loaded): " + count), false);
        return count;
    }

    private static int reportAllLoaded(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork network = FactoryNetwork.get(level);
        MinecraftServer server = level.getServer();
        int belts = network.getLoadedBeltCount(server);
        int producers = network.getLoadedProducerCount(server);
        int consumers = network.getLoadedConsumerCount(server);
        int machines = network.getLoadedMachineCount(server);
        int containers = network.getLoadedContainerCount(server);
        int total = belts + producers + consumers + machines + containers;

        ctx.getSource().sendSuccess(() -> Component.literal(
                ("""
                        
                        Factory components in loaded chunks:
                        Belts: %d, Producers: %d, Consumers: %d, Machines: %d, Containers: %d
                        Total: %d""")
                        .formatted(belts, producers, consumers, machines, containers, total)), false);
        return total;
    }

    private static int setFrozen(CommandContext<CommandSourceStack> ctx, boolean frozen) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork network = FactoryNetwork.get(level);
        network.setFrozen(frozen);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Factory ticking is now " + (frozen ? "frozen. This state does not persist between restarts." : "unfrozen.")), true);
        return 1;
    }
}