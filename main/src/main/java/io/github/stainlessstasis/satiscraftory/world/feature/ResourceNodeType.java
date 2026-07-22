package io.github.stainlessstasis.satiscraftory.world.feature;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.valueproviders.IntProvider;

public record ResourceNodeType(
        String name,        // "iron", "whatever"
        Item rawItem,
        Block oreBlock,
        IntProvider radius, // UniformInt.of(n1, n2)
        int rarity          // onAverageOnceEvery(n)
) {
    public Identifier nodeId(String modId) {
        return Identifier.fromNamespaceAndPath(modId, name + "_resource_node");
    }
}