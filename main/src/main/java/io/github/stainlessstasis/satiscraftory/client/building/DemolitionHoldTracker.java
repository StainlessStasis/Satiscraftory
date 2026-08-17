package io.github.stainlessstasis.satiscraftory.client.building;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.building.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.building.demolition.DemolitionSelectionManager;
import io.github.stainlessstasis.satiscraftory.network.serverbound.DemolitionHoldPingPacket;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class DemolitionHoldTracker {
    private static @Nullable SoundInstance holdSound;
    private static boolean wasActive = false;

    private DemolitionHoldTracker() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            setActive(mc, false);
            return;
        }

        boolean heldBuildGun = player.getMainHandItem().getItem() instanceof BuildGunItem;
        boolean aimingAtNothing = !(mc.hitResult instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK;
        boolean hasSelection = !DemolitionSelectionManager.clientSelection().isEmpty();
        boolean holding = mc.options.keyAttack.isDown();

        boolean active = holding && heldBuildGun && aimingAtNothing && hasSelection;
        setActive(mc, active);

        if (active) {
            int ticks = Math.min(DemolitionSelectionManager.clientHoldTicks() + 1, DemolitionSelectionManager.HOLD_TICKS_TO_DEMOLISH);
            DemolitionSelectionManager.setClientHoldTicks(ticks);
            ClientPacketDistributor.sendToServer(new DemolitionHoldPingPacket());
        } else {
            DemolitionSelectionManager.setClientHoldTicks(0);
        }
    }

    private static void setActive(Minecraft mc, boolean active) {
        if (active && !wasActive) {
            holdSound = SimpleSoundInstance.forUI(SCSounds.BUILD_GUN_DEMOLISH.value(), 1f);
            mc.getSoundManager().play(holdSound);
        } else if (!active && wasActive && holdSound != null) {
            mc.getSoundManager().stop(holdSound);
            holdSound = null;
        }
        wasActive = active;
    }
}