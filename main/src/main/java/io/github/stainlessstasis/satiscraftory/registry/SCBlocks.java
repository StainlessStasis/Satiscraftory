package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.util.BeltConstants;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.MinerBlock;
import io.github.stainlessstasis.satiscraftory.block.ResourceNodeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SCBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Satiscraftory.MODID);

    public static final DeferredBlock<ResourceNodeBlock> RESOURCE_NODE = BLOCKS.registerBlock("resource_node", ResourceNodeBlock::new);
    public static final DeferredBlock<MinerBlock> MINER_MK1 = BLOCKS.registerBlock(
            "miner_mk1",
            MinerBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).noLootTable()
    );

    public static final DeferredBlock<BeltBlock> BELT_MK1 = registerBelt("belt_mk1", 1f/20);   // 60/min
    public static final DeferredBlock<BeltBlock> BELT_MK2 = registerBelt("belt_mk2", 2f/20);   // 120/min
    public static final DeferredBlock<BeltBlock> BELT_MK3 = registerBelt("belt_mk3", 4.5f/20); // 270/min

    private static DeferredBlock<BeltBlock> registerBelt(String id, float speed) {
        return BLOCKS.registerBlock(id,
                properties -> new BeltBlock(properties, BeltConstants.getScaledBeltSpeed(speed)),
                () -> BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .mapColor(MapColor.STONE)
                        .strength(2.0f)
        );
    }
}
