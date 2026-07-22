package io.github.stainlessstasis.satiscraftory.registry;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class SCResourceNodes {
    public static final ResourceNodeType IRON = new ResourceNodeType("iron", Items.RAW_IRON, Blocks.IRON_ORE, UniformInt.of(3, 5), 200);
    public static final ResourceNodeType COPPER = new ResourceNodeType("copper", Items.RAW_COPPER, Blocks.COPPER_ORE, UniformInt.of(3, 5), 200);

    public static final List<ResourceNodeType> TYPES = List.of(IRON, COPPER);
}