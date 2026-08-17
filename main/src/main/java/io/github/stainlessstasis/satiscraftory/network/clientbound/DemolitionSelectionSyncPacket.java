package io.github.stainlessstasis.satiscraftory.network.clientbound;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record DemolitionSelectionSyncPacket(List<BlockPos> selection) implements CustomPacketPayload {
    public static final Type<DemolitionSelectionSyncPacket> TYPE = new Type<>(Satiscraftory.id("demolition_selection_sync"));

    public static final StreamCodec<ByteBuf, DemolitionSelectionSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(DemolitionSelectionManager.MAX_SELECTION)),
            DemolitionSelectionSyncPacket::selection,
            DemolitionSelectionSyncPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(DemolitionSelectionSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> DemolitionSelectionManager.applyClientSync(packet.selection()));
    }
}