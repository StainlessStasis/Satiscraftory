package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.registry.block.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.block.SCBlocks;
import io.github.stainlessstasis.satiscraftory.registry.world.SCBiomeModifiers;
import io.github.stainlessstasis.satiscraftory.registry.world.SCFeatures;
import io.github.stainlessstasis.satiscraftory.registry.world.SCResourceNodes;
import net.neoforged.bus.api.IEventBus;

public class SCRegistries {
    public static void register(IEventBus bus) {
        var _ignored = SCResourceNodes.TYPES; // force classloading to make shit work
        SCBlocks.BLOCKS.register(bus);
        SCBlockEntities.BLOCK_ENTITIES.register(bus);
        SCItems.ITEMS.register(bus);
        SCFeatures.FEATURES.register(bus);
        SCSounds.SOUND_EVENTS.register(bus);
        SCMenus.MENUS.register(bus);
        SCBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(bus);
    }
}
