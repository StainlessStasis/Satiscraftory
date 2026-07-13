package io.github.stainlessstasis.manifold.factory;

import io.github.stainlessstasis.manifold.command.FactoryCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class FactoryEvents {
    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        var overworld = event.getServer().overworld();
        var tickRateManger = overworld.tickRateManager();
        boolean isTicking = tickRateManger.runsNormally() || tickRateManger.isSteppingForward();
        if (isTicking) {
            FactoryNetwork.get(overworld).tickAll(overworld, overworld.getGameTime());
        }
    }

    @SubscribeEvent
     static void onRegisterCommands(RegisterCommandsEvent event) {
        FactoryCommands.register(event.getDispatcher());
    }
}
