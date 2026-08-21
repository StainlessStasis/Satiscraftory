package io.github.stainlessstasis.satiscraftory.network.serverbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneMarker;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

/**
 * Tells the server which L-shaped route the client is currently previewing
 */
public record LaneAxisHintPacket(boolean primaryIsX) implements CustomPacketPayload {
    public static final Type<LaneAxisHintPacket> TYPE = new Type<>(Satiscraftory.id("lane_axis_hint"));

    public static final StreamCodec<ByteBuf, LaneAxisHintPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, LaneAxisHintPacket::primaryIsX,
            LaneAxisHintPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(LaneAxisHintPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;
            LaneMarker.setAxisHint(player, packet.primaryIsX());
        });
    }
}