package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class SCResourceNodes {
    public static final List<ResourceNodeType> TYPES = List.of(
            new ResourceNodeType("iron", Items.RAW_IRON, Blocks.IRON_ORE, UniformInt.of(3, 5), 200),
            new ResourceNodeType("copper", Items.RAW_COPPER, Blocks.COPPER_ORE, UniformInt.of(3, 5), 200)
    );
}