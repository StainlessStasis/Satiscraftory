package io.github.stainlessstasis.satiscraftory;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = Satiscraftory.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Satiscraftory.MODID, value = Dist.CLIENT)
public class SatiscraftoryClient {
    public SatiscraftoryClient(ModContainer container) {
//        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {}
}
