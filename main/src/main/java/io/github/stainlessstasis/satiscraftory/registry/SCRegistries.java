package io.github.stainlessstasis.satiscraftory.registry;

import net.neoforged.bus.api.IEventBus;

public class SCRegistries {
    public static void register(IEventBus bus) {
        var _ignored = SCResourceNodes.TYPES; // force classloading to make shit work
        SCBlocks.BLOCKS.register(bus);
        SCBlockEntities.BLOCK_ENTITIES.register(bus);
        SCItems.ITEMS.register(bus);
        SCFeatures.FEATURES.register(bus);
    }
}
