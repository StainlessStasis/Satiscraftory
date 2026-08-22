package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.manifold.factory_component.Laneable;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionResolver;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionTarget;
import io.github.stainlessstasis.satiscraftory.network.serverbound.LaneBuildModeTogglePacket;
import io.github.stainlessstasis.satiscraftory.network.serverbound.SelectBuildingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class BuildGunClientEvents {
    private BuildGunClientEvents(){}

    @SubscribeEvent
    static void onPickBlock(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getKeyMapping() != mc.options.keyPickItem) return;

        LocalPlayer player = mc.player;
        if (player == null || !(player.level() instanceof ClientLevel level)) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        if (!(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK) return;

        DemolitionTarget target = DemolitionResolver.resolve(level, blockHit.getBlockPos(), false);
        if (target == null) return;

        event.setCanceled(true);

        Identifier itemId = BuiltInRegistries.ITEM.getKey(target.canonicalItem());
        ClientPacketDistributor.sendToServer(new SelectBuildingPacket(itemId));
    }

    @SubscribeEvent
    static void onSwapHands(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) return;
        if (!(player.getMainHandItem().getItem() instanceof BuildGunItem)) return;

        BlockItem selected = BuildGunItem.getSelectedBlockItemClientSide();
        if (!(selected.getBlock() instanceof Laneable)) return;

        while (mc.options.keySwapOffhand.consumeClick()) {
            ClientPacketDistributor.sendToServer(new LaneBuildModeTogglePacket());
        }
    }
}