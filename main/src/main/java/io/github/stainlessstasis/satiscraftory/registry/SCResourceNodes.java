package io.github.stainlessstasis.satiscraftory.registry;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class SCResourceNodes {
    // TODO: custom neo registry
    public static final ResourceNodeType IRON = new ResourceNodeType(
            "iron", Items.RAW_IRON, Blocks.IRON_ORE,
            UniformInt.of(3, 5), UniformInt.of(1, 3), UniformInt.of(20, 30),
            200
    );
    public static final ResourceNodeType COPPER = new ResourceNodeType(
            "copper", Items.RAW_COPPER, Blocks.COPPER_ORE,
            UniformInt.of(3, 5), UniformInt.of(1, 3), UniformInt.of(20, 30),
            200
    );

    public static final List<ResourceNodeType> TYPES = List.of(IRON, COPPER);

    public static ResourceNodeType byName(String name) {
        return TYPES.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    public static ResourceNodeType byBlock(Block block) {
        return TYPES.stream().filter(t -> t.getNodeBlock().get() == block).findFirst().orElse(null);
    }
}