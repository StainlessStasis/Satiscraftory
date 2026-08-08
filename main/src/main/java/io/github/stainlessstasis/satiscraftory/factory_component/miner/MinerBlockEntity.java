package io.github.stainlessstasis.satiscraftory.factory_component.miner;

import io.github.stainlessstasis.manifold.animation.AnimationPhase;
import io.github.stainlessstasis.manifold.animation.AnimationPhaseTransition;
import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlock;
import io.github.stainlessstasis.manifold.factory_component.producer.ProducerBlockEntity;
import io.github.stainlessstasis.manifold.factory_component.producer.Producer;
import io.github.stainlessstasis.manifold.factory_power.CableAnchorProvider;
import io.github.stainlessstasis.manifold.multiblock.MultiblockControllerAccess;
import io.github.stainlessstasis.manifold.util.DirectionalOffset;
import io.github.stainlessstasis.manifold.util.FactorySounds;
import io.github.stainlessstasis.manifold.util.TickDebouncer;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import io.github.stainlessstasis.satiscraftory.registry.SCSounds;
import io.github.stainlessstasis.satiscraftory.resource_node.ResourceNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MinerBlockEntity extends ProducerBlockEntity implements MultiblockControllerAccess, CableAnchorProvider, AnimationPhaseTransition {
    private static final double DEMAND_MW = 5d;
    private static final int BUFFER_FULL_THRESHOLD_TICKS = 100;

    private @Nullable BlockPos linkedNodePos = null;
    private @Nullable Identifier resourceNodeId = null;

    public final MinerAnimationStates animationStates = new MinerAnimationStates();
    public AnimationPhase animationPhase = AnimationPhase.IDLE;
    public boolean hasDescended = false;

    private final TickDebouncer bufferFullDebouncer = new TickDebouncer(false, BUFFER_FULL_THRESHOLD_TICKS);
    private boolean isPowered = false;
    private boolean previousPowered = false;

    public static final Vec3 PARTICLE_LOCAL_OFFSET = new Vec3(0, 0, -4);
    public static final long PARTICLE_INTERVAL_MS = 10L;
    public static final double PARTICLE_JITTER = 0.3d;
    private final Vec3 particleOffset;
    private long lastParticleTime = -1L;

    public static final Vec3 CABLE_ANCHOR_LOCAL_OFFSET = new Vec3(-11, 139, -63.5).scale(1/16f);
    private final Vec3 cableAnchorPos;

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        this(SCBlockEntities.MINER.get(), pos, state);
    }

    public MinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        this.particleOffset = DirectionalOffset.toWorld(facing, PARTICLE_LOCAL_OFFSET);
        this.cableAnchorPos = new Vec3(getBlockPos()).add(getCableOffset(state, CABLE_ANCHOR_LOCAL_OFFSET));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            linkToResourceNode(serverLevel);
            registerPowerConsumer(serverLevel);
        }
    }

    @Override
    public double getPowerDemand() {
        return DEMAND_MW;
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
        Producer producer = getFactoryComponent();
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

    @Override
    public void onEnterStartup(long gameTime) {
        if (!hasDescended) {
            animationStates.startupDescend.start((int) gameTime);
        } else {
            animationStates.startupAlreadyDescended.start((int) gameTime);
        }
        FactorySounds.playLocal(this, particleOffset, SCSounds.MINER_STARTUP.value(), 0.67f, 1f);
    }

    @Override
    public void onEnterLoop(long gameTime) {
        animationStates.startupDescend.stop();
        animationStates.startupAlreadyDescended.stop();
        hasDescended = true;
        lastParticleTime = -1L;
    }

    @Override
    public void onEnterCooldown(long gameTime) {
        FactorySounds.playLocal(this, particleOffset, SCSounds.MINER_COOLDOWN.value(), 0.67f, 1f);
    }

    public void setLastParticleTime(long animationMilliseconds) {
        lastParticleTime = animationMilliseconds;
    }

    public long getLastParticleTime() {
        return lastParticleTime;
    }

    public Vec3 getParticleOffset() {
        return particleOffset;
    }


    public boolean isBufferFull() {
        if (level instanceof ServerLevel) {
            return getFactoryComponent().isBufferFull();
        } else {
            return bufferFullDebouncer.get();
        }
    }

    public boolean isPowered() {
        if (level instanceof ServerLevel) {
            return getFactoryComponent().isPowered();
        } else {
            return isPowered;
        }
    }

    @Override
    public Vec3 getCableAnchorPos() {
        return cableAnchorPos;
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, MinerBlockEntity miner) {
        Producer producer = miner.getFactoryComponent();
        if (producer == null) return;

        if (miner.bufferFullDebouncer.update(producer.isBufferFull())) {
            miner.syncToClients();
        }

        boolean powered = producer.isPowered();
        if (powered != miner.previousPowered) {
            miner.isPowered = powered;
            miner.previousPowered = powered;
            miner.syncToClients();
        }

        miner.tickPowerIndicator(level);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        if (resourceNodeId != null) {
            output.putString("ResourceNodeId", resourceNodeId.toString());
        }
        output.putBoolean("IsBlocked", bufferFullDebouncer.get());
        output.putBoolean("IsPowered", isPowered);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        String resourceNodeString = input.getStringOr("ResourceNodeId", "");
        if (!resourceNodeString.isEmpty()) {
            resourceNodeId = Identifier.parse(resourceNodeString);
        }
        bufferFullDebouncer.restore(input.getBooleanOr("IsBlocked", false));
        isPowered = input.getBooleanOr("IsPowered", false);
        previousPowered = isPowered;
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