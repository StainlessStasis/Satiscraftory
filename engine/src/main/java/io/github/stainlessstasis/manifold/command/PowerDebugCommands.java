package io.github.stainlessstasis.manifold.command;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory.PowerGrid;
import io.github.stainlessstasis.manifold.factory.PowerNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;

import java.util.List;
import java.util.function.Predicate;

public final class PowerDebugCommands {
    private static final Predicate<CommandSourceStack> GAMEMASTER = source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

    private PowerDebugCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("power")
                .requires(GAMEMASTER)

                .then(Commands.literal("link")
                        .then(Commands.argument("posA", BlockPosArgument.blockPos())
                                .then(Commands.argument("posB", BlockPosArgument.blockPos())
                                        .executes(ctx -> link(ctx,
                                                BlockPosArgument.getBlockPos(ctx, "posA"),
                                                BlockPosArgument.getBlockPos(ctx, "posB"))))))

                .then(Commands.literal("unlink")
                        .then(Commands.argument("posA", BlockPosArgument.blockPos())
                                .then(Commands.argument("posB", BlockPosArgument.blockPos())
                                        .executes(ctx -> unlink(ctx,
                                                BlockPosArgument.getBlockPos(ctx, "posA"),
                                                BlockPosArgument.getBlockPos(ctx, "posB"))))))

                .then(Commands.literal("node")
                        .then(Commands.literal("add")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> addNode(ctx, BlockPosArgument.getBlockPos(ctx, "pos")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> removeNode(ctx, BlockPosArgument.getBlockPos(ctx, "pos"))))))

                .then(Commands.literal("which")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> which(ctx, BlockPosArgument.getBlockPos(ctx, "pos")))))

                .then(Commands.literal("networks")
                        .executes(PowerDebugCommands::listNetworks))

                .then(Commands.literal("supply")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> setSupply(ctx,
                                                BlockPosArgument.getBlockPos(ctx, "pos"),
                                                DoubleArgumentType.getDouble(ctx, "amount"))))))

                .then(Commands.literal("demand")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                        .executes(ctx -> setDemand(ctx,
                                                BlockPosArgument.getBlockPos(ctx, "pos"),
                                                DoubleArgumentType.getDouble(ctx, "amount"))))))

                .then(Commands.literal("status")
                        .executes(PowerDebugCommands::status))

                .then(Commands.literal("tick")
                        .executes(PowerDebugCommands::manualTick))
        );
    }

    private static int addNode(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.addNode(GlobalPos.of(level.dimension(), pos));
        ctx.getSource().sendSuccess(() -> Component.literal("Added power node at " + pos.toShortString()), false);
        return 1;
    }

    private static int removeNode(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.removeNode(GlobalPos.of(level.dimension(), pos));
        ctx.getSource().sendSuccess(() -> Component.literal("Removed power node at " + pos.toShortString()), false);
        return 1;
    }

    private static int link(CommandContext<CommandSourceStack> ctx, BlockPos a, BlockPos b) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.addEdge(GlobalPos.of(level.dimension(), a), GlobalPos.of(level.dimension(), b));
        ctx.getSource().sendSuccess(() ->
                Component.literal("Linked " + a.toShortString() + " <-> " + b.toShortString()), false);
        return 1;
    }

    private static int unlink(CommandContext<CommandSourceStack> ctx, BlockPos a, BlockPos b) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.removeEdge(GlobalPos.of(level.dimension(), a), GlobalPos.of(level.dimension(), b));
        ctx.getSource().sendSuccess(() ->
                Component.literal("Unlinked " + a.toShortString() + " <-> " + b.toShortString()), false);
        return 1;
    }

    private static int which(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        PowerNetwork network = grid.networkOf(GlobalPos.of(level.dimension(), pos));

        if (network == null) {
            ctx.getSource().sendSuccess(() ->
                    Component.literal(pos.toShortString() + " is not a registered power node"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    pos.toShortString() + " is in network " + network.getId()
                            + " (" + network.size() + " node" + (network.size() == 1 ? "" : "s") + ")"), false);
        }
        return 1;
    }

    private static int setSupply(CommandContext<CommandSourceStack> ctx, BlockPos pos, double amount) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.registerProducer(GlobalPos.of(level.dimension(), pos), amount);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Set supply " + amount + " at " + pos.toShortString()), false);
        return 1;
    }

    private static int setDemand(CommandContext<CommandSourceStack> ctx, BlockPos pos, double amount) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        grid.registerConsumer(GlobalPos.of(level.dimension(), pos), amount, null);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Set demand " + amount + " at " + pos.toShortString()), false);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        List<PowerNetwork> networks = grid.getNetworks();

        if (networks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No power networks exist"), false);
            return 0;
        }

        for (PowerNetwork network : networks) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Network " + network.getId() + " (" + network.size() + " node(s)):"), false);
            for (var member : network.getMembers()) {
                double satisfaction = grid.getSatisfaction(member);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "  " + member.pos().toShortString()
                                + " satisfaction=" + String.format("%.0f%%", satisfaction * 100)
                                + " powered=" + grid.isPowered(member)), false);
            }
        }
        return networks.size();
    }

    private static int manualTick(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        FactoryNetwork.get(level).getPowerGrid().tick();
        ctx.getSource().sendSuccess(() -> Component.literal("Ticked power grid"), false);
        return 1;
    }

    private static int listNetworks(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        PowerGrid grid = FactoryNetwork.get(level).getPowerGrid();
        List<PowerNetwork> networks = grid.getNetworks();

        if (networks.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("No power networks exist"), false);
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(networks.size() + " power network(s):"), false);
        for (PowerNetwork network : networks) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "  " + network.getId() + " - " + network.size() + " node(s): " + network.getMembers()), false);
        }
        return networks.size();
    }
}