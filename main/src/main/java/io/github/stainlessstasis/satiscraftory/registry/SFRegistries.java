package io.github.stainlessstasis.satiscraftory.registry;

import net.neoforged.bus.api.IEventBus;

public class SFRegistries {
    public static void register(IEventBus bus) {
        SFBlocks.BLOCKS.register(bus);
        SFItems.ITEMS.register(bus);
    }
}
