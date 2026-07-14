package io.github.stainlessstasis.manifold;

import io.github.stainlessstasis.manifold.network.NetworkSetup;
import io.github.stainlessstasis.manifold.registry.ManifoldRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Manifold.MODID)
public class Manifold {
    public static final String MODID = "manifold";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Manifold(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(NetworkSetup::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ManifoldRegistries.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
