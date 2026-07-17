package io.github.stainlessstasis.manifold.command;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;

import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

public final class FactoryCommands {
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
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
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
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(ctx -> setFrozen(ctx, true)))
                .then(Commands.literal("unfreeze")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .executes(ctx -> setFrozen(ctx, false)))
        );
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