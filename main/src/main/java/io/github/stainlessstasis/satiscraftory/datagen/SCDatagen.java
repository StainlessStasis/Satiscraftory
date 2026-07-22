package io.github.stainlessstasis.satiscraftory.datagen;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class SCDatagen {
    @SubscribeEvent
    static void datagen(GatherDataEvent.Client event) {
        event.createProvider(SCModelProvider::new);
        event.createProvider(SCMachineRecipesProvider::new);
        event.createProvider((output, lookupProvider) ->
                new SCBlockTagsProvider(output, lookupProvider, Satiscraftory.MODID)
        );
        event.createDatapackRegistryObjects(SCWorldgenBootstrap.BUILDER);
    }
}
