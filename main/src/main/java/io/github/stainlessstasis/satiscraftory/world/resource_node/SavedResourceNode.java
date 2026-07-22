package io.github.stainlessstasis.satiscraftory.world.resource_node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.satiscraftory.block_entity.ResourceNodePurity;
import io.github.stainlessstasis.satiscraftory.registry.ResourceNodeType;
import io.github.stainlessstasis.satiscraftory.registry.SCResourceNodes;
import net.minecraft.core.GlobalPos;

public record SavedResourceNode(GlobalPos pos, ResourceNodeType type, ResourceNodePurity purity) {
    public static final Codec<SavedResourceNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(SavedResourceNode::pos),
            Codec.STRING.xmap(SCResourceNodes::byName, ResourceNodeType::getName).fieldOf("type").forGetter(SavedResourceNode::type),
            ResourceNodePurity.CODEC.fieldOf("purity").forGetter(SavedResourceNode::purity)
    ).apply(instance, SavedResourceNode::new));
}
