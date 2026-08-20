package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneMarker;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record LaneStartSyncPacket(@Nullable BlockPos pos) implements CustomPacketPayload {
    public static final Type<LaneStartSyncPacket> TYPE = new Type<>(Satiscraftory.id("lane_start_sync"));

    public static final StreamCodec<ByteBuf, LaneStartSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), LaneStartSyncPacket::posOptional,
            packet -> new LaneStartSyncPacket(packet.orElse(null))
    );

    private Optional<BlockPos> posOptional() {
        return Optional.ofNullable(pos);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(LaneStartSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> LaneMarker.applyClientSync(packet.pos()));
    }
}