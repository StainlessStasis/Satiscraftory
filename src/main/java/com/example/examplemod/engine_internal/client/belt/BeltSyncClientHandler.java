package com.example.examplemod.engine_internal.client.belt;

import com.example.examplemod.engine_internal.block_entity.BeltBlockEntity;
import com.example.examplemod.engine_internal.network.BeltSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class BeltSyncClientHandler {
    public static void handle(BeltSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            for (BeltSyncPacket.Entry entry : payload.entries()) {
                if (level.getBlockEntity(entry.pos()) instanceof BeltBlockEntity beltBlockEntity) {
                    beltBlockEntity.applySync(entry.items(), entry.syncTick());
                }
            }
        });
    }
}
