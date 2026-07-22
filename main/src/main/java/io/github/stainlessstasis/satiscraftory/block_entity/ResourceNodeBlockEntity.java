package io.github.stainlessstasis.satiscraftory.block_entity;

import io.github.stainlessstasis.manifold.factory_component.PayloadItems;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class ResourceNodeBlockEntity extends BlockEntity {
    public static final Identifier DEFAULT_RESOURCE_TYPE = ItemUtils.idOf(Items.RAW_IRON);
    public static final ResourceNodePurity DEFAULT_PURITY = ResourceNodePurity.NORMAL;

    private Identifier resourceType = DEFAULT_RESOURCE_TYPE;
    private ResourceNodePurity purity = DEFAULT_PURITY;

    public ResourceNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ResourceNodeBlockEntity(BlockPos pos, BlockState state) {
        super(SCBlockEntities.RESOURCE_NODE_MARKER.get(), pos, state);
    }

    public Identifier getResourceType() {
        return resourceType;
    }

    public void setResourceType(Identifier resourceType) {
        this.resourceType = resourceType;
        setChanged();
    }

    public ResourceNodePurity getPurity() {
        return purity;
    }

    public void setPurity(ResourceNodePurity purity) {
        this.purity = purity;
        setChanged();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putString("ResourceType", resourceType.toString());
        output.putString("Purity", purity.name());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        resourceType = Identifier.parse(
                input.getStringOr("ResourceType", DEFAULT_RESOURCE_TYPE.toString())
        );
        purity = ResourceNodePurity.valueOf(
                input.getStringOr("Purity", DEFAULT_PURITY.name())
        );
    }
}