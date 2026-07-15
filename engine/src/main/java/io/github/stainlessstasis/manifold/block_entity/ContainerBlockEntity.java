package io.github.stainlessstasis.manifold.block_entity;

import io.github.stainlessstasis.manifold.factory.FactoryLinking;
import io.github.stainlessstasis.manifold.factory.FactoryNetwork;
import io.github.stainlessstasis.manifold.factory_component.Container;
import io.github.stainlessstasis.manifold.factory_component.Payload;
import io.github.stainlessstasis.manifold.factory_component.PayloadItems;
import io.github.stainlessstasis.manifold.menu.ContainerMenu;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class ContainerBlockEntity extends BlockEntity implements net.minecraft.world.Container, MenuProvider {
    public static final int SLOT_COUNT = 27;

    private Container container;

    public ContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ManifoldBlockEntities.CONTAINER.get(), pos, state);
    }

    public ContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel serverLevel)) return;

        FactoryNetwork network = FactoryNetwork.get(serverLevel);
        container = network.getOrCreateContainer(
                GlobalPos.of(serverLevel.dimension(), getBlockPos()), () -> new Container(SLOT_COUNT));

        relink(network);
        FactoryLinking.relinkNeighbors(serverLevel, getBlockPos());
    }

    public void relink(FactoryNetwork network) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        GlobalPos selfPos = GlobalPos.of(serverLevel.dimension(), getBlockPos());
        Direction outputDirection = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos outputPos = getBlockPos().relative(outputDirection);
        network.linkContainerOutput(selfPos, GlobalPos.of(serverLevel.dimension(), outputPos), outputDirection);
    }

    public void onNeighborChanged() {
        if (level instanceof ServerLevel serverLevel) relink(FactoryNetwork.get(serverLevel));
    }

    public Container getContainer() {
        return container;
    }


    @Override
    public int getContainerSize() {
        return container.getSlotCount();
    }

    @Override
    public boolean isEmpty() {
        return container.isEmpty();
    }

    @Override
    public @NonNull ItemStack getItem(int slot) {
        Payload payload = container.getSlot(slot);
        if (payload == null) return ItemStack.EMPTY;
        ItemStack stack = PayloadItems.toItemStack(payload);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItem(int slot, int amount) {
        Payload payload = container.getSlot(slot);
        if (payload == null || amount <= 0) return ItemStack.EMPTY;

        int taken = Math.min(amount, payload.count());
        ItemStack result = PayloadItems.toItemStack(payload.itemId(), taken);
        int remaining = payload.count() - taken;
        container.setSlot(slot, remaining > 0 ? payload.withCount(remaining) : null);
        setChanged();
        return result != null ? result : ItemStack.EMPTY;
    }

    @Override
    public @NonNull ItemStack removeItemNoUpdate(int slot) {
        Payload payload = container.getSlot(slot);
        container.setSlot(slot, null);
        setChanged();
        if (payload == null) return ItemStack.EMPTY;
        ItemStack result = PayloadItems.toItemStack(payload);
        return result != null ? result : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        container.setSlot(slot, stack.isEmpty() ? null : PayloadItems.fromItemStack(stack));
        setChanged();
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel serverLevel) FactoryNetwork.get(serverLevel).setDirty();
    }

    @Override
    public void clearContent() {
        container.clear();
        setChanged();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (level == null || level.getBlockEntity(getBlockPos()) != this) return false;
        return player.distanceToSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) <= 64;
    }

    @Override
    public @NonNull Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NonNull Inventory playerInventory, @NonNull Player player) {
        return new ContainerMenu(containerId, playerInventory, this);
    }
}