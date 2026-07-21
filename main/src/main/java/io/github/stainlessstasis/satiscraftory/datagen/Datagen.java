package io.github.stainlessstasis.satiscraftory.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class Datagen {
    @SubscribeEvent
    static void datagen(GatherDataEvent.Client event) {
        event.createProvider(SCModelProvider::new);
        event.createProvider(MachineRecipesProvider::new);
    }
}
