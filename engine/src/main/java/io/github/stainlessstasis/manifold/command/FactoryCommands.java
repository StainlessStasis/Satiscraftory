package io.github.stainlessstasis.manifold.command;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

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
}