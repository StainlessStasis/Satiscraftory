package io.github.stainlessstasis.manifold.network;

import io.github.stainlessstasis.manifold.Manifold;
import io.github.stainlessstasis.manifold.client.factory_power.ClientChainState;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record ChainStateSyncPacket(Optional<BlockPos> chainStartPos) implements CustomPacketPayload {
    public static final Type<ChainStateSyncPacket> TYPE = new Type<>(Manifold.id("chain_state_sync"));

    public static final StreamCodec<ByteBuf, ChainStateSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), ChainStateSyncPacket::chainStartPos,
            ChainStateSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ChainStateSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientChainState.setChainStart(packet.chainStartPos().orElse(null)));
    }
}