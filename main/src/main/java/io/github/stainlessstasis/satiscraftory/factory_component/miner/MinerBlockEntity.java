package io.github.stainlessstasis.satiscraftory.factory_component.miner;

import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlock;
import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.util.DirectionalOffset;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodeBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MinerBlockEntity extends ProducerBlockEntity implements MultiblockControllerAccess {
    private @Nullable BlockPos linkedNodePos = null;
    private @Nullable Identifier resourceNodeId = null;
    public final AnimationState startupAnimationState = new AnimationState();
    public final AnimationState spinAnimationState = new AnimationState();

    public static final Vec3 PARTICLE_LOCAL_OFFSET = new Vec3(0, 0, -4);
    public static final long PARTICLE_INTERVAL_MS = 10L;
    public static final double PARTICLE_JITTER = 0.3d;
    private final Vec3 particleOffset;
    private long lastParticleTime = -1L;
    private long lastDrillLoopCycle = -1L;

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        this(SCBlockEntities.MINER.get(), pos, state);
    }

    public MinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        this.particleOffset = DirectionalOffset.toWorld(facing, PARTICLE_LOCAL_OFFSET);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            linkToResourceNode(serverLevel);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel serverLevel) {
            unlinkFromResourceNode(serverLevel);
        }
    }

    @Override
    public List<BlockPos> getMultiblockFillerPositions() {
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return MinerBlock.MULTIBLOCK_SHAPE.absoluteFillerPositions(getBlockPos(), facing);
    }

    private void linkToResourceNode(ServerLevel level) {
        BlockPos nodePos = MinerBlock.findNearbyResourceNode(level, getBlockPos());
        if (nodePos == null) return;
        if (!(level.getBlockEntity(nodePos) instanceof ResourceNodeBlockEntity nodeBE)) return;
        if (!nodeBE.tryAssignMiner(getBlockPos())) return;

        linkedNodePos = nodePos.immutable();
        resourceNodeId = nodeBE.getNodeTypeId();
        syncToClients();
        Producer producer = getProducer();
        if (producer == null) return;

        producer.setItemId(nodeBE.getResourceType());
        long baseInterval = getBlockState().getBlock() instanceof ProducerBlock producerBlock
                ? producerBlock.getIntervalTicks()
                : Producer.DEFAULT_INTERVAL_TICKS;
        float multiplier = nodeBE.getPurity().getProductionRateMultiplier();
        long effectiveInterval = Math.max(1, Math.round(baseInterval / multiplier));
        producer.setInterval(effectiveInterval);
    }

    public void unlinkFromResourceNode(ServerLevel level) {
        if (linkedNodePos == null) return;
        if (level.getBlockEntity(linkedNodePos) instanceof ResourceNodeBlockEntity nodeBE) {
            nodeBE.unassignMiner(getBlockPos());
        }
        linkedNodePos = null;
        resourceNodeId = null;
        syncToClients();
    }

    public @Nullable BlockPos getLinkedNodePos() {
        return linkedNodePos;
    }

    public @Nullable Identifier getResourceNodeId() {
        return resourceNodeId;
    }

    public void setLastParticleTime(long animationMilliseconds) {
        lastParticleTime = animationMilliseconds;
    }

    public long getLastParticleTime() {
        return lastParticleTime;
    }

    public long getLastDrillLoopCycle() {
        return lastDrillLoopCycle;
    }

    public void setLastDrillLoopCycle(long lastDrillLoopCycle) {
        this.lastDrillLoopCycle = lastDrillLoopCycle;
    }

    public Vec3 getParticleOffset() {
        return particleOffset;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        if (resourceNodeId != null) {
            output.putString("ResourceNodeId", resourceNodeId.toString());
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        String resourceNodeString = input.getStringOr("ResourceNodeId", "");
        System.out.println(resourceNodeString);
        if (!resourceNodeString.isEmpty()) {
            resourceNodeId = Identifier.parse(resourceNodeString);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void syncToClients() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}