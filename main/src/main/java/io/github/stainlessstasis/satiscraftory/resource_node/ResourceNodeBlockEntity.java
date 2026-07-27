package io.github.stainlessstasis.satiscraftory.resource_node;

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
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ResourceNodeBlockEntity extends BlockEntity {
    public static final Identifier DEFAULT_RESOURCE_TYPE = ItemUtils.idOf(Items.RAW_IRON);
    public static final ResourceNodePurity DEFAULT_PURITY = ResourceNodePurity.NORMAL;

    private Identifier resourceType = DEFAULT_RESOURCE_TYPE;
    private @Nullable Identifier nodeTypeId = null;
    private ResourceNodePurity purity = DEFAULT_PURITY;
    private @Nullable BlockPos minerPos = null;

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

    public @Nullable Identifier getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(Identifier nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
        setChanged();
    }

    public boolean isOccupied() {
        return minerPos != null;
    }

    public boolean tryAssignMiner(BlockPos minerPos) {
        if (this.minerPos != null && !this.minerPos.equals(minerPos)) return false;
        this.minerPos = minerPos.immutable();
        setChanged();
        return true;
    }

    public void unassignMiner(BlockPos minerPos) {
        if (this.minerPos != null && this.minerPos.equals(minerPos)) {
            this.minerPos = null;
            setChanged();
        }
    }

    public Optional<BlockPos> getMinerPos() {
        return Optional.ofNullable(minerPos);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putString("ResourceType", resourceType.toString());
        output.putString("Purity", purity.name());
        if (nodeTypeId != null) {
            output.putString("NodeTypeId", nodeTypeId.toString());
        }
        getMinerPos().ifPresent(pos -> output.putLong("MinerPos", pos.asLong()));
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        resourceType = Identifier.parse(input.getStringOr("ResourceType", DEFAULT_RESOURCE_TYPE.toString()));
        purity = ResourceNodePurity.valueOf(input.getStringOr("Purity", DEFAULT_PURITY.name()));
        String nodeTypeString = input.getStringOr("NodeTypeId", "");
        nodeTypeId = nodeTypeString.isEmpty() ? null : Identifier.parse(nodeTypeString);

        long storedMinerPos = input.getLongOr("MinerPos", Long.MIN_VALUE);
        minerPos = storedMinerPos == Long.MIN_VALUE ? null : BlockPos.of(storedMinerPos).immutable();
    }
}