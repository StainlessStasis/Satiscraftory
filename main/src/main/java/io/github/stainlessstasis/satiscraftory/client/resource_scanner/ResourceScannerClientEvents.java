package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import io.github.stainlessstasis.manifold.client.radial_menu.RadialMenuOption;
import io.github.stainlessstasis.manifold.client.radial_menu.RadialMenuScreen;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.SatiscraftoryConfig;
import io.github.stainlessstasis.satiscraftory.item.ResourceScannerItem;
import io.github.stainlessstasis.satiscraftory.network.serverbound.SelectScanTargetPacket;
import io.github.stainlessstasis.satiscraftory.registry.world.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.apache.commons.lang3.StringUtils;

@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public final class ResourceScannerClientEvents {

    @SubscribeEvent
    static void onLogout(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientResourceScanState.clear();
    }

    @SubscribeEvent
    static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() == Minecraft.getInstance().player) {
            ClientResourceScanState.clear();
        }
    }


    @SubscribeEvent
    static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getKeyMapping() != mc.options.keyUse) return;

        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof ResourceScannerItem)) return;

        event.setCanceled(true);
        openRadialMenu();
    }

    @SubscribeEvent
    static void render(RenderLevelStageEvent.AfterLevel event) {
        int scanRange = SatiscraftoryConfig.RESOURCE_SCANNER_RANGE.get();
        scanRange = Math.min(scanRange, 2048);
        double scanDurationTicks = scanRange / ClientResourceScanState.PING_SPEED_BLOCKS_PER_TICK;
        float scanDurationMillis = (float) (scanDurationTicks / 20f * 1000f);
        ScanEffectRenderer.INSTANCE.render(scanRange, scanDurationMillis);
    }

    private static void openRadialMenu() {
        var options = SCResourceNodes.TYPES.stream()
                .map(ResourceScannerClientEvents::toOption)
                .toList();

        RadialMenuScreen.open(options, (ResourceNodeType selected) ->
                ClientPacketDistributor.sendToServer(new SelectScanTargetPacket(selected.getNodeId()))
        );
    }

    private static RadialMenuOption<ResourceNodeType> toOption(ResourceNodeType type) {
        ItemStack icon = new ItemStack(type.getResourceBlock().asItem());

        String resourceName = StringUtils.capitalize(type.getName());
        return new RadialMenuOption<>(type, icon, Component.literal(resourceName));
    }
}