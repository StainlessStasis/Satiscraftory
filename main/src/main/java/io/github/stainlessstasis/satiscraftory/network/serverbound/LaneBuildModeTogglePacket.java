package io.github.stainlessstasis.satiscraftory.network.serverbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.lane.LaneBuildModeManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record LaneBuildModeTogglePacket() implements CustomPacketPayload {
    public static final Type<LaneBuildModeTogglePacket> TYPE = new Type<>(Satiscraftory.id("lane_build_mode_toggle"));

    public static final StreamCodec<ByteBuf, LaneBuildModeTogglePacket> STREAM_CODEC =
            StreamCodec.unit(new LaneBuildModeTogglePacket());

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(LaneBuildModeTogglePacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;
            LaneBuildModeManager.toggle(player);
        });
    }
}