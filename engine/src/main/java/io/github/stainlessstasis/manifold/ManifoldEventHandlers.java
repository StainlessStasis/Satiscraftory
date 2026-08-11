package io.github.stainlessstasis.manifold;

import io.github.stainlessstasis.manifold.command.FactoryCommands;
import io.github.stainlessstasis.manifold.command.PowerDebugCommands;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.item.power_link.PowerLinkItem;
import io.github.stainlessstasis.manifold.recipe.ManifoldGeneratorFuels;
import io.github.stainlessstasis.manifold.recipe.ManifoldMachineRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Manifold.MODID)
public class ManifoldEventHandlers {
    @SubscribeEvent
    static void onTick(ServerTickEvent.Post event) {
        var overworld = event.getServer().overworld();
        var tickRateManger = overworld.tickRateManager();
        boolean isTicking = tickRateManger.runsNormally() || tickRateManger.isSteppingForward();
        if (isTicking) {
            FactoryNetwork network = FactoryNetwork.get(overworld);
            if (!network.isFrozen()) {
                network.tickAll(overworld, overworld.getGameTime());
            }
        }
    }

    @SubscribeEvent
    static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        FactoryCommands.register(dispatcher);
        PowerDebugCommands.register(dispatcher);
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Manifold.id(ManifoldMachineRecipes.PATH), new ManifoldMachineRecipes());
        event.addListener(Manifold.id(ManifoldGeneratorFuels.PATH), new ManifoldGeneratorFuels());
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PowerLinkItem.clearChain(serverPlayer);
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PowerLinkItem.resync(serverPlayer);
        }
    }

    @SubscribeEvent
    static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PowerLinkItem.resync(serverPlayer);
        }
    }
}