package com.example.examplemod.engine_internal.datagen;

import com.example.examplemod.ExampleMod;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class InternalEngineDatagen {
    @SubscribeEvent
    static void datagen(GatherDataEvent.Client event) {
        event.createProvider(InternalEngineModelProvider::new);
    }
}
