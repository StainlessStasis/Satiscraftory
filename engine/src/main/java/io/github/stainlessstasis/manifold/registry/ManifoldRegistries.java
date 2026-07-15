package io.github.stainlessstasis.manifold.registry;

import net.neoforged.bus.api.IEventBus;

public class ManifoldRegistries {
    public static void register(IEventBus bus) {
        ManifoldBlocks.BLOCKS.register(bus);
        ManifoldBlockEntities.BLOCK_ENTITIES.register(bus);
        ManifoldItems.ITEMS.register(bus);
        ManifoldMenus.MENUS.register(bus);
    }
}
