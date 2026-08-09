package io.github.stainlessstasis.manifold.item.power_link;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.network.PowerGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CableCutterItem extends PowerLinkItem {
    private static final Map<UUID, GlobalPos> selectionByPlayer = new HashMap<>();

    public CableCutterItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer) || !(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos resolvedBlockPos = PowerLinkTargets.resolve(serverLevel, context.getClickedPos());
        if (resolvedBlockPos == null) {
            return InteractionResult.PASS;
        }
        GlobalPos resolvedGlobalPos = GlobalPos.of(serverLevel.dimension(), resolvedBlockPos);

        if (player.isCrouching()) {
            selectionByPlayer.remove(player.getUUID());
            player.sendOverlayMessage(Component.literal("Cut selection cleared"));
            return InteractionResult.SUCCESS_SERVER;
        }

        PowerGrid powerGrid = FactoryNetwork.get(serverLevel).getPowerGrid();
        if (!powerGrid.getNodes().contains(resolvedGlobalPos)) {
            player.sendOverlayMessage(Component.literal("That's not connected to the power grid"));
            return InteractionResult.FAIL;
        }

        GlobalPos selectionStart = selectionByPlayer.get(player.getUUID());

        if (selectionStart == null) {
            selectionByPlayer.put(player.getUUID(), resolvedGlobalPos);
            player.sendOverlayMessage(Component.literal(
                    "Selected " + resolvedBlockPos.toShortString() + " - click the other end of a cable to cut it")
            );
            return InteractionResult.SUCCESS_SERVER;
        }

        if (selectionStart.equals(resolvedGlobalPos)) {
            player.sendOverlayMessage(Component.literal("Select a different block to cut a connection"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!powerGrid.hasEdge(selectionStart, resolvedGlobalPos)) {
            player.sendOverlayMessage(Component.literal("Those blocks aren't directly connected"));
            selectionByPlayer.put(player.getUUID(), resolvedGlobalPos);
            return InteractionResult.FAIL;
        }

        powerGrid.removeEdge(selectionStart, resolvedGlobalPos);
        selectionByPlayer.remove(player.getUUID());
        player.sendOverlayMessage(Component.literal(
                "Cut cable " + selectionStart.pos().toShortString() + " -> " + resolvedBlockPos.toShortString()));
        return InteractionResult.SUCCESS_SERVER;
    }
}