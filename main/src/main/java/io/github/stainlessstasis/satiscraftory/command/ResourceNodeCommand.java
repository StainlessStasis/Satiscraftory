package io.github.stainlessstasis.satiscraftory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.stainlessstasis.satiscraftory.registry.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.world.resource_node.ResourceNodeData;
import io.github.stainlessstasis.satiscraftory.world.resource_node.SavedResourceNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

@EventBusSubscriber
public class ResourceNodeCommand {

    private static final SuggestionProvider<CommandSourceStack> TYPE_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(SCResourceNodes.TYPES.stream().map(ResourceNodeType::getName), builder);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("findnode")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests(TYPE_SUGGESTIONS)
                        .executes(ResourceNodeCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String typeName = StringArgumentType.getString(context, "type");
        ResourceNodeType type = SCResourceNodes.byName(typeName);
        if (type == null) {
            context.getSource().sendFailure(Component.literal("Unknown resource node type: " + typeName));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos playerPos = player.blockPosition();
        ResourceNodeData data = ResourceNodeData.get(player.level());

        List<SavedResourceNode> matches = data.getNodesOfType(type).stream()
                .filter(node -> node.pos().dimension().equals(player.level().dimension()))
                .sorted(Comparator.comparingDouble(node -> node.pos().pos().distSqr(playerPos)))
                .limit(5)
                .toList();

        if (matches.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No known " + typeName + " nodes in this dimension"), false);
            return 0;
        }

        context.getSource().sendSuccess(() -> {
            StringBuilder string = new StringBuilder("Nearest " + typeName + " nodes:");
            for (SavedResourceNode node : matches) {
                BlockPos pos = node.pos().pos();
                string.append(String.format("\n %s (%.0f blocks)", pos.toShortString(), Math.sqrt(pos.distSqr(playerPos))));
            }
            return Component.literal(string.toString());
        }, false);

        return matches.size();
    }
}