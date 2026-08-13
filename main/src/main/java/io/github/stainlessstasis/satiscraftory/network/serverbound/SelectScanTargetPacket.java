package io.github.stainlessstasis.satiscraftory.network.serverbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import io.github.stainlessstasis.satiscraftory.item.ResourceNodeScannerItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record SelectScanTargetPacket(Identifier nodeTypeId) implements CustomPacketPayload {
    public static final Type<SelectScanTargetPacket> TYPE = new Type<>(Satiscraftory.id("select_scan_target"));

    public static final StreamCodec<ByteBuf, SelectScanTargetPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SelectScanTargetPacket::nodeTypeId,
            SelectScanTargetPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SelectScanTargetPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            if (!(player.getMainHandItem().getItem() instanceof ResourceNodeScannerItem)) return;

            ResourceNodeType type = SCResourceNodes.byNodeId(packet.nodeTypeId());
            if (type == null) return;

            ResourceNodeScannerItem.setSelectedType(player, packet.nodeTypeId());
            if (player.level() instanceof ServerLevel serverLevel) {
                ResourceNodeScannerItem.performScan(player, serverLevel, type);
            }
        });
    }
}