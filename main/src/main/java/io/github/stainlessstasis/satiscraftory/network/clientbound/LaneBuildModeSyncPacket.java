package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildMode;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record LaneBuildModeSyncPacket(LaneBuildMode mode) implements CustomPacketPayload {
    public static final Type<LaneBuildModeSyncPacket> TYPE =
            new Type<>(io.github.stainlessstasis.satiscraftory.Satiscraftory.id("lane_build_mode_sync"));

    public static final StreamCodec<ByteBuf, LaneBuildModeSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(LaneBuildMode::valueOf, Enum::name), LaneBuildModeSyncPacket::mode,
            LaneBuildModeSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(LaneBuildModeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> LaneBuildModeManager.applyClientSync(packet.mode()));
    }
}