package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.manifold.block.belt.BeltBlock;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Satiscraftory.MODID);

    public static final DeferredBlock<BeltBlock> BELT_MK1 = BLOCKS.registerBlock("belt_mk1",
            properties -> new BeltBlock(properties, 1/20f),
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
    );

    public static final DeferredBlock<BeltBlock> BELT_MK2 = BLOCKS.registerBlock("belt_mk2",
            properties -> new BeltBlock(properties, 2/20f),
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
    );

    public static final DeferredBlock<BeltBlock> BELT_MK3 = BLOCKS.registerBlock("belt_mk3",
            properties -> new BeltBlock(properties, 4.5/20f),
            () -> BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f)
    );
}
