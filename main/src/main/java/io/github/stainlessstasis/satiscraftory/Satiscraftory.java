package io.github.stainlessstasis.satiscraftory;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Satiscraftory.MODID)
@EventBusSubscriber
public class Satiscraftory {
    public static final String MODID = "satiscraftory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Satiscraftory(IEventBus modEventBus, ModContainer modContainer) {
        CREATIVE_MODE_TABS.register(modEventBus);
//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("creative_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup."+MODID))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ManifoldItems.MACHINE.get().getDefaultInstance())
            .displayItems((_, output) -> {
                output.accept(ManifoldItems.PRODUCER.get());
                output.accept(ManifoldItems.BELT_MK1.get());
                output.accept(ManifoldItems.BELT_MK2.get());
                output.accept(ManifoldItems.MACHINE.get());
                output.accept(ManifoldItems.CONTAINER.get());
                output.accept(ManifoldItems.CONSUMER.get());
            }).build()
    );
}
