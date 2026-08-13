package io.github.stainlessstasis.satiscraftory;

import io.github.stainlessstasis.manifold.registry.ManifoldItems;
import io.github.stainlessstasis.satiscraftory.building.BuildingCosts;
import io.github.stainlessstasis.satiscraftory.registry.SCItems;
import io.github.stainlessstasis.satiscraftory.registry.SCRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Satiscraftory.MODID)
@EventBusSubscriber(modid = Satiscraftory.MODID)
public class Satiscraftory {
    public static final String MODID = "satiscraftory";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Satiscraftory(IEventBus modEventBus, ModContainer modContainer) {
        CREATIVE_MODE_TABS.register(modEventBus);
        SCRegistries.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, SatiscraftoryConfig.SPEC);
    }

    @SubscribeEvent
    static void commonSetup(FMLCommonSetupEvent event) {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FACTORY_COMPONENTS_TAB = CREATIVE_MODE_TABS.register("factory_components", () -> CreativeModeTab.builder()
            .title(Component.translatable("factory_components_tab."+MODID))
            .withTabsBefore(CreativeModeTabs.OP_BLOCKS)
            .icon(() -> ManifoldItems.MACHINE.get().getDefaultInstance())
            .displayItems((_, output) -> {
                SCItems.getFactoryBlockItems().forEach(item -> output.accept(item.get()));
                output.accept(ManifoldItems.SPLITTER.get());
                output.accept(ManifoldItems.MERGER.get());
                output.accept(ManifoldItems.MACHINE.get());
                output.accept(ManifoldItems.CONTAINER.get());
                output.accept(ManifoldItems.CONSUMER.get());
                output.accept(ManifoldItems.POWER_PRODUCER.get());
            }).build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FACTORY_ITEMS_TAB = CREATIVE_MODE_TABS.register("factory_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("factory_items_tab."+MODID))
            .withTabsBefore(FACTORY_COMPONENTS_TAB.getKey())
            .icon(() -> SCItems.SCREWS.get().getDefaultInstance())
            .displayItems((_, output) -> {
                output.accept(SCItems.BUILD_GUN);
                output.accept(SCItems.RESOURCE_NODE_SCANNER);
                output.accept(ManifoldItems.CABLE_CUTTER);
                SCItems.getFactoryItems().forEach(item -> output.accept(item.get()));
            }).build()
    );

    @SubscribeEvent
    static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Satiscraftory.id(BuildingCosts.PATH), new BuildingCosts());
    }
}