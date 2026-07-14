package io.github.stainlessstasis.satiscraftory;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Satiscraftory.MODID)
public class Satiscraftory {
    public static final String MODID = "satiscraftory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Satiscraftory(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
