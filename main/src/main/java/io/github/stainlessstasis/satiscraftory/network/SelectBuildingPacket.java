package io.github.stainlessstasis.satiscraftory.network;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildingCatalog;
import io.github.stainlessstasis.satiscraftory.item.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.progression.TierUnlocks;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record SelectBuildingPacket(Identifier buildingItemId) implements CustomPacketPayload {
    public static final Type<SelectBuildingPacket> TYPE = new Type<>(Satiscraftory.id("select_building"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, SelectBuildingPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SelectBuildingPacket::buildingItemId,
            SelectBuildingPacket::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SelectBuildingPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        context.enqueueWork(() -> {
            if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

            BuildingCatalog.BuildingEntry entry = BuildingCatalog.byId(packet.buildingItemId()).orElse(null);
            if (entry == null) return;

            player.level();
            boolean unlocked = TierUnlocks.isUnlockedOnServer(player.level().getServer(), entry.unlock());
            if (!unlocked) return;

            BuildGunItem.setSelectedBlock(player, entry.id());
        });
    }
}