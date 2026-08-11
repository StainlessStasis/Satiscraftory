package io.github.stainlessstasis.satiscraftory.network;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlock;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;
import java.util.List;

public record ProgressionSyncPacket(int tier, List<TierUnlock> unlocks) implements CustomPacketPayload {
    public static final Type<ProgressionSyncPacket> TYPE = new Type<>(Satiscraftory.id("progression_sync"));

    public static final StreamCodec<ByteBuf, ProgressionSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ProgressionSyncPacket::tier,
            TierUnlock.STREAM_CODEC.apply(ByteBufCodecs.list()), ProgressionSyncPacket::unlocks,
            ProgressionSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ProgressionSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var unlocks = EnumSet.noneOf(TierUnlock.class);
            unlocks.addAll(packet.unlocks());
            TierUnlocks.applySync(packet.tier(), unlocks);
        });
    }
}