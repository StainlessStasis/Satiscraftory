package io.github.stainlessstasis.satiscraftory.registry.world;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.valueproviders.TrapezoidInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class SCResourceNodes {
    // TODO: custom neo registry
    public static final ResourceNodeType IRON = new ResourceNodeType(
            "iron", Items.RAW_IRON, Blocks.IRON_ORE,
            UniformInt.of(3, 5), TrapezoidInt.of(1, 4, 2), UniformInt.of(20, 30),
            500
    );
    public static final ResourceNodeType COPPER = new ResourceNodeType(
            "copper", Items.RAW_COPPER, Blocks.COPPER_ORE,
            UniformInt.of(3, 5), TrapezoidInt.of(1, 4, 2), UniformInt.of(20, 30),
            500
    );

    public static final List<ResourceNodeType> TYPES = List.of(IRON, COPPER);

    public static ResourceNodeType byName(String name) {
        return TYPES.stream().filter(resourceNodeType -> resourceNodeType.getName().equals(name)).findFirst().orElse(null);
    }

    public static ResourceNodeType byBlock(Block block) {
        return TYPES.stream().filter(resourceNodeType -> resourceNodeType.getNodeBlock().get() == block).findFirst().orElse(null);
    }

    public static ResourceNodeType byNodeId(Identifier nodeId) {
        return TYPES.stream().filter(resourceNodeType -> resourceNodeType.getNodeId().equals(nodeId)).findFirst().orElse(null);
    }

    public static ParticleOptions particleFor(Identifier resourceNodeId) {
        ResourceNodeType type = TYPES.stream()
                .filter(resourceNodeType -> resourceNodeType.getNodeId().equals(resourceNodeId))
                .findFirst()
                .orElse(null);

        Block block = type != null ? type.getResourceBlock() : Blocks.STONE;
        return new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState());
    }
}