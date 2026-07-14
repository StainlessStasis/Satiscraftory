package io.github.stainlessstasis.manifold;

import io.github.stainlessstasis.manifold.command.FactoryCommands;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.recipe.ManifoldRecipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ManifoldEventHandlers {
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

    @SubscribeEvent
    static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Manifold.id("recipes"), new ManifoldRecipes());
    }
}
