package com.example.examplemod.engine;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class FactoryEvents {
    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        FactoryNetwork.get(server.overworld()).tickAll(server.getTickCount());
    }
}
