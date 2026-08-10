package io.github.stainlessstasis.satiscraftory;

import io.github.stainlessstasis.manifold.client.multiblock.PreviewHeldItemSource;
import io.github.stainlessstasis.satiscraftory.client.biomass_burner.BiomassBurnerModel;
import io.github.stainlessstasis.satiscraftory.client.biomass_burner.BiomassBurnerRenderer;
import io.github.stainlessstasis.satiscraftory.client.miner.MinerModel;
import io.github.stainlessstasis.satiscraftory.client.miner.MinerRenderer;
import io.github.stainlessstasis.satiscraftory.client.miner.MinerScreen;
import io.github.stainlessstasis.satiscraftory.client.power_pole.PowerPoleModel;
import io.github.stainlessstasis.satiscraftory.client.power_pole.PowerPoleRenderer;
import io.github.stainlessstasis.satiscraftory.item.BuildGunItem;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCMenus;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = Satiscraftory.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public class SatiscraftoryClient {
    public SatiscraftoryClient(ModContainer container) {
//        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PreviewHeldItemSource.register(player -> {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof BuildGunItem) {
                return new ItemStack(BuildGunItem.getSelectedBlockItem(player));
            }
            return ItemStack.EMPTY;
        });
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MinerModel.LAYER_LOCATION, MinerModel::createBodyLayer);
        event.registerLayerDefinition(PowerPoleModel.LAYER_LOCATION, PowerPoleModel::createBodyLayer);
        event.registerLayerDefinition(BiomassBurnerModel.LAYER_LOCATION, BiomassBurnerModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SCBlockEntities.MINER.get(), MinerRenderer::new);
        event.registerBlockEntityRenderer(SCBlockEntities.POWER_POLE.get(), PowerPoleRenderer::new);
        event.registerBlockEntityRenderer(SCBlockEntities.BIOMASS_BURNER.get(), BiomassBurnerRenderer::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(SCMenus.MINER.get(), MinerScreen::new);
    }
}