package io.github.stainlessstasis.manifold.item;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.PowerGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowerLinkDebugItem extends Item {
    private static final Map<UUID, GlobalPos> chainStartByPlayer = new HashMap<>();

    public PowerLinkDebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos clickedBlockPos = context.getClickedPos();
        GlobalPos clickedGlobalPos = GlobalPos.of(serverLevel.dimension(), clickedBlockPos);

        if (player.isCrouching()) {
            chainStartByPlayer.remove(player.getUUID());
            player.sendOverlayMessage(Component.literal("Power link chain reset"));
            return InteractionResult.SUCCESS_SERVER;
        }

        PowerGrid powerGrid = FactoryNetwork.get(serverLevel).getPowerGrid();
        GlobalPos chainStartPos = chainStartByPlayer.get(player.getUUID());

        if (chainStartPos == null) {
            powerGrid.addNode(clickedGlobalPos);
            chainStartByPlayer.put(player.getUUID(), clickedGlobalPos);
            player.sendOverlayMessage(Component.literal(
                    "Power link chain started at " + clickedBlockPos.toShortString()));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (chainStartPos.equals(clickedGlobalPos)) {
            player.sendOverlayMessage(Component.literal("That's already the current chain endpoint"));
            return InteractionResult.SUCCESS_SERVER;
        }

        powerGrid.addEdge(chainStartPos, clickedGlobalPos);
        spawnLinkParticles(serverLevel, chainStartPos.pos(), clickedBlockPos);
        chainStartByPlayer.put(player.getUUID(), clickedGlobalPos);

        player.sendOverlayMessage(Component.literal(
                "Linked " + chainStartPos.pos().toShortString() + " -> " + clickedBlockPos.toShortString()
                        + " (sneak-click to start a new chain)"));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void spawnLinkParticles(ServerLevel serverLevel, BlockPos fromBlockPos, BlockPos toBlockPos) {
        Vec3 fromCenter = Vec3.atCenterOf(fromBlockPos);
        Vec3 toCenter = Vec3.atCenterOf(toBlockPos);

        double distance = fromCenter.distanceTo(toCenter);
        int segmentCount = Math.max(1, (int) Math.round(distance * 4));

        for (int segmentIndex = 0; segmentIndex <= segmentCount; segmentIndex++) {
            double progress = (double) segmentIndex / segmentCount;
            Vec3 point = fromCenter.lerp(toCenter, progress);
            serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    public static void clearChain(@Nullable UUID playerId) {
        if (playerId != null) chainStartByPlayer.remove(playerId);
    }
}