package io.github.stainlessstasis.satiscraftory.registry;

import io.github.stainlessstasis.manifold.block.factory_component.belt.BeltBlock;
import io.github.stainlessstasis.manifold.util.BeltConstants;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.block.MinerBlock;
import io.github.stainlessstasis.satiscraftory.block.ResourceNodeBlock;
import io.github.stainlessstasis.satiscraftory.world.feature.ResourceNodeType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.stream.Collectors;

public class SCBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Satiscraftory.MODID);

    public static final Map<String, DeferredBlock<ResourceNodeBlock>> RESOURCE_NODES =
            SCResourceNodes.TYPES.stream().collect(Collectors.toMap(
                    ResourceNodeType::name,
                    type -> registerResourceNode(type.name(), type.rawItem())
            ));

    public static DeferredBlock<ResourceNodeBlock> resourceNode(String name) {
        return RESOURCE_NODES.get(name);
    }
    public static final DeferredBlock<MinerBlock> MINER_MK1 = registerMiner("miner_mk1", 20L); // 60/min

    public static final DeferredBlock<BeltBlock> BELT_MK1 = registerBelt("belt_mk1", 1f/20);   // 60/min
    public static final DeferredBlock<BeltBlock> BELT_MK2 = registerBelt("belt_mk2", 2f/20);   // 120/min
    public static final DeferredBlock<BeltBlock> BELT_MK3 = registerBelt("belt_mk3", 4.5f/20); // 270/min

    private static DeferredBlock<BeltBlock> registerBelt(String id, float speed) {
        return BLOCKS.registerBlock(id,
                properties -> new BeltBlock(properties, BeltConstants.getScaledBeltSpeed(speed)),
                () -> BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .mapColor(MapColor.STONE)
                        .strength(2f)
        );
    }

    private static DeferredBlock<ResourceNodeBlock> registerResourceNode(String name, Item resource) {
        return BLOCKS.registerBlock(name+"_resource_node",
                properties -> new ResourceNodeBlock(properties, ItemUtils.idOf(resource)),
                () -> BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(-1f, 360_000f)
                        .noLootTable()
        );
    }

    private static DeferredBlock<MinerBlock> registerMiner(String id, long intervalTicks) {
        return BLOCKS.registerBlock(id,
                properties -> new MinerBlock(properties, intervalTicks),
                () -> BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(2f)
        );
    }
}
