package io.github.stainlessstasis.manifold.registry;

import net.neoforged.bus.api.IEventBus;

public class InternalEngineRegistries {
    public static void register(IEventBus bus) {
        InternalEngineBlocks.BLOCKS.register(bus);
        InternalEngineBlockEntities.BLOCK_ENTITIES.register(bus);
        InternalEngineItems.ITEMS.register(bus);
    }
}
