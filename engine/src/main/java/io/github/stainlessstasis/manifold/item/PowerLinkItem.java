package io.github.stainlessstasis.manifold.item;

import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_power.PowerGrid;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerRegistry;
import io.github.stainlessstasis.manifold.network.ChainStateSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PowerLinkItem extends Item {
    private static final Map<UUID, GlobalPos> chainStartByPlayer = new HashMap<>();

    public PowerLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer) || !(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        BlockPos resolvedBlockPos = resolveLinkTarget(serverLevel, context.getClickedPos());
        if (resolvedBlockPos == null) {
            player.sendOverlayMessage(Component.literal("That block can't be connected to the power grid"));
            return InteractionResult.FAIL;
        }
        GlobalPos resolvedGlobalPos = GlobalPos.of(serverLevel.dimension(), resolvedBlockPos);

        if (player.isCrouching()) {
            setChainStart(serverPlayer, null);
            player.sendOverlayMessage(Component.literal("Power link chain reset"));
            return InteractionResult.SUCCESS_SERVER;
        }

        PowerGrid powerGrid = FactoryNetwork.get(serverLevel).getPowerGrid();
        GlobalPos chainStartPos = chainStartByPlayer.get(player.getUUID());

        if (chainStartPos == null) {
            Component rejection = validateChainStart(serverLevel, resolvedGlobalPos);
            if (rejection != null) {
                player.sendOverlayMessage(rejection);
                return InteractionResult.FAIL;
            }

            powerGrid.addNode(resolvedGlobalPos);
            setChainStart(serverPlayer, resolvedGlobalPos);
            player.sendOverlayMessage(Component.literal(
                    "Power link chain started at " + resolvedBlockPos.toShortString()));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (chainStartPos.equals(resolvedGlobalPos)) {
            player.sendOverlayMessage(Component.literal("That's already the current chain endpoint"));
            return InteractionResult.SUCCESS_SERVER;
        }

        ItemStack heldStack = context.getItemInHand();
        Component rejection = validateLink(serverLevel, chainStartPos, resolvedGlobalPos, heldStack);
        if (rejection != null) {
            player.sendOverlayMessage(rejection);
            return InteractionResult.FAIL;
        }

        boolean alreadyConnected = powerGrid.hasEdge(chainStartPos, resolvedGlobalPos);
        if (!powerGrid.addEdge(chainStartPos, resolvedGlobalPos)) {
            player.sendOverlayMessage(Component.literal(
                    "One end of that connection is already at its cable limit"));
            return InteractionResult.FAIL;
        }

        spawnLinkParticles(serverLevel, chainStartPos.pos(), resolvedBlockPos);
        if (!alreadyConnected) onLinkCreated(context, chainStartPos, resolvedGlobalPos);

        setChainStart(serverPlayer, resolvedGlobalPos);

        player.sendOverlayMessage(Component.literal(
                (alreadyConnected ? "Already linked " : "Linked ")
                        + chainStartPos.pos().toShortString() + " -> " + resolvedBlockPos.toShortString()
                        + " (sneak-click to start a new chain)"));
        return InteractionResult.SUCCESS_SERVER;
    }

    protected @Nullable BlockPos resolveLinkTarget(ServerLevel level, BlockPos clickedPos) {
        BlockPos mbControllerPos = MultiblockFillerRegistry.controllerPosAt(level, clickedPos);
        return mbControllerPos != null ? mbControllerPos : clickedPos;
    }

    protected @Nullable Component validateChainStart(ServerLevel level, GlobalPos pos) {
        return null;
    }

    protected @Nullable Component validateLink(ServerLevel level, GlobalPos fromPos, GlobalPos toPos, ItemStack heldStack) {
        return null;
    }

    protected void onLinkCreated(UseOnContext context, GlobalPos fromPos, GlobalPos toPos) {}

    protected static void spawnLinkParticles(ServerLevel serverLevel, BlockPos fromBlockPos, BlockPos toBlockPos) {
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

    public static void setChainStart(ServerPlayer player, @Nullable GlobalPos globalPos) {
        if (globalPos == null) {
            chainStartByPlayer.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new ChainStateSyncPacket(Optional.empty()));
        } else {
            chainStartByPlayer.put(player.getUUID(), globalPos);
            PacketDistributor.sendToPlayer(player, new ChainStateSyncPacket(Optional.of(globalPos.pos())));
        }
    }

    public static void clearChain(@Nullable ServerPlayer player) {
        if (player != null) {
            setChainStart(player, null);
        }
    }

    public static void resync(ServerPlayer player) {
        GlobalPos pos = chainStartByPlayer.get(player.getUUID());
        BlockPos blockPos = pos != null && pos.dimension().equals(player.level().dimension()) ? pos.pos() : null;
        PacketDistributor.sendToPlayer(player, new ChainStateSyncPacket(Optional.ofNullable(blockPos)));
    }
}