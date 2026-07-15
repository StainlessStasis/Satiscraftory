package io.github.stainlessstasis.manifold;

import io.github.stainlessstasis.manifold.client.belt.BeltRenderer;
import io.github.stainlessstasis.manifold.menu.ContainerScreen;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import io.github.stainlessstasis.manifold.registry.ManifoldMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Manifold.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Manifold.MODID, value = Dist.CLIENT)
public class ManifoldClient {
    public ManifoldClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {}

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ManifoldBlockEntities.BELT.get(), BeltRenderer::new);
    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ManifoldMenus.CONTAINER.get(), ContainerScreen::new);
    }
}
