package io.github.stainlessstasis.manifold.factory_component.producer;

import io.github.stainlessstasis.manifold.factory_component.AbstractDirectionalFactoryBlock;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.manifold.util.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ProducerBlock extends AbstractDirectionalFactoryBlock {
    public static final MapCodec<ProducerBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.LONG.fieldOf("interval_ticks").forGetter(ProducerBlock::getIntervalTicks),
                    propertiesCodec()
            ).apply(instance, (intervalTicks, properties) -> new ProducerBlock(properties, intervalTicks))
    );

    private final long intervalTicks;

    public ProducerBlock(Properties properties, long intervalTicks) {
        super(properties);
        this.intervalTicks = intervalTicks;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new ProducerBlockEntity(ManifoldBlockEntities.PRODUCER.get(), pos, state);
    }

    @Override
    protected void notifyNeighborChanged(BlockEntity blockEntity, ServerLevel level) {
        if (blockEntity instanceof ProducerBlockEntity producerBE) producerBE.onNeighborChanged();
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level, @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        FactoryNetwork.get(level).removeProducer(GlobalPos.of(level.dimension(), pos));
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        //noinspection DataFlowIssue - gamemode is nullable but should not be null here
        if (!(level instanceof ServerLevel serverLevel) || !player.gameMode().isCreative()) return InteractionResult.PASS;

        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        Producer producer = FactoryNetwork.get(serverLevel).getProducer(globalPos);
        if (producer == null) return InteractionResult.PASS;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            if (player.isCrouching()) {
                producer.setInterval(producer.getInterval() - 1);
            } else {
                producer.setInterval(producer.getInterval() + 1);
            }
            player.sendOverlayMessage(Component.literal("Set producer interval to "+producer.getInterval()));
            return InteractionResult.SUCCESS_SERVER;
        } else {
            Item item = heldItem.getItem();
            producer.setItemId(item);
            player.sendOverlayMessage(Component.literal("Set producer item to "+ ItemUtils.idOf(item)));
        }

        return InteractionResult.PASS;
    }
}