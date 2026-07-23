package io.github.stainlessstasis.satiscraftory;

import io.github.stainlessstasis.satiscraftory.client.miner.MinerModel;
import io.github.stainlessstasis.satiscraftory.client.miner.MinerRenderer;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Satiscraftory.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public class SatiscraftoryClient {
    public SatiscraftoryClient(ModContainer container) {
//        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {}

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MinerModel.LAYER_LOCATION, MinerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SCBlockEntities.MINER.get(), MinerRenderer::new);
    }
}
