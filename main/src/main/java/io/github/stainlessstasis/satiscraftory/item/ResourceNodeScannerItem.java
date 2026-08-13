package io.github.stainlessstasis.satiscraftory.item;

import io.github.stainlessstasis.manifold.util.MessageUtil;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
import io.github.stainlessstasis.satiscraftory.network.clientbound.ResourceScanResultPacket;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodeData;
import io.github.stainlessstasis.satiscraftory.resource_node.SavedResourceNode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourceNodeScannerItem extends Item {
    private static final ResourceNodeType DEFAULT_SCAN_TYPE = SCResourceNodes.IRON;
    private static final int MAX_RESULTS = 24;
    private static final Map<UUID, Identifier> selectedTypeByPlayer = new HashMap<>();

    public ResourceNodeScannerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        performScan(serverPlayer, serverLevel, getSelectedType(player));
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void performScan(ServerPlayer player, ServerLevel level, ResourceNodeType type) {
        int range = SatiscraftoryConfig.RESOURCE_NODE_SCANNER_RANGE.get();

        var results = ResourceNodeData.get(level)
                .findNearby(type, level.dimension(), player.blockPosition(), range, MAX_RESULTS);

        if (results.isEmpty()) {
            MessageUtil.warnPlayer(player, Satiscraftory.MODID + ".resource_scanner.no_nodes_found", type.getName());
            return;
        }

        var scannedNodes = results.stream()
                .map(ResourceNodeScannerItem::toScannedNode)
                .toList();

        PacketDistributor.sendToPlayer(player, new ResourceScanResultPacket(type.getNodeId(), scannedNodes));
    }

    private static ResourceScanResultPacket.ScannedNode toScannedNode(SavedResourceNode node) {
        return new ResourceScanResultPacket.ScannedNode(node.pos().pos(), node.purity());
    }

    public static void setSelectedType(Player player, Identifier nodeTypeId) {
        selectedTypeByPlayer.put(player.getUUID(), nodeTypeId);
    }

    public static @Nullable Identifier getSelectedTypeId(Player player) {
        return selectedTypeByPlayer.get(player.getUUID());
    }

    public static ResourceNodeType getSelectedType(Player player) {
        Identifier selectedId = selectedTypeByPlayer.get(player.getUUID());
        if (selectedId != null) {
            ResourceNodeType resolved = SCResourceNodes.byNodeId(selectedId);
            if (resolved != null) return resolved;
        }
        return DEFAULT_SCAN_TYPE;
    }
}