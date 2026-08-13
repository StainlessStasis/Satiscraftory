package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.network.serverbound.DemolitionHoldPingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class DemolitionHoldTracker {
    private DemolitionHoldTracker() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean heldBuildGun = player.getMainHandItem().getItem() instanceof BuildGunItem;
        boolean aimingAtNothing = !(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK;
        boolean hasSelection = !DemolitionSelectionManager.clientSelection().isEmpty();
        boolean holding = mc.options.keyAttack.isDown();

        if (holding && heldBuildGun && aimingAtNothing && hasSelection) {
            int ticks = Math.min(DemolitionSelectionManager.clientHoldTicks() + 1, DemolitionSelectionManager.HOLD_TICKS_TO_DEMOLISH);
            DemolitionSelectionManager.setClientHoldTicks(ticks);
            ClientPacketDistributor.sendToServer(new DemolitionHoldPingPacket());
        } else {
            DemolitionSelectionManager.setClientHoldTicks(0);
        }
    }
}