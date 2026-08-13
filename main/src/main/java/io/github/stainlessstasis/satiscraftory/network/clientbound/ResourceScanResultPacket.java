package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.client.resource_scanner.ClientResourceScanState;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodePurity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record ResourceScanResultPacket(Identifier nodeTypeId, List<ScannedNode> nodes) implements CustomPacketPayload {
    public static final Type<ResourceScanResultPacket> TYPE = new Type<>(Satiscraftory.id("resource_scan_result"));

    public static final StreamCodec<ByteBuf, ResourceScanResultPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ResourceScanResultPacket::nodeTypeId,
            ScannedNode.STREAM_CODEC.apply(ByteBufCodecs.list()), ResourceScanResultPacket::nodes,
            ResourceScanResultPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResourceScanResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientResourceScanState.setResults(packet.nodeTypeId(), packet.nodes()));
    }

    public record ScannedNode(BlockPos pos, ResourceNodePurity purity) {
        public static final StreamCodec<ByteBuf, ScannedNode> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, ScannedNode::pos,
                ResourceNodePurity.STREAM_CODEC, ScannedNode::purity,
                ScannedNode::new
        );
    }
}