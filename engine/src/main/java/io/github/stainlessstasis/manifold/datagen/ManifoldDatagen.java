package io.github.stainlessstasis.manifold.datagen;

import io.github.stainlessstasis.manifold.Manifold;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Manifold.MODID)
public class ManifoldDatagen {
    @SubscribeEvent
    static void datagen(GatherDataEvent.Client event) {
        event.createProvider(ManifoldModelProvider::new);
        event.createProvider(MachineRecipeProvider::new);
    }
}
