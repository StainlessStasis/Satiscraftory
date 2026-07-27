package io.github.stainlessstasis.manifold.mixin;

import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerBlock;
import io.github.stainlessstasis.manifold.multiblock.MultiblockFillerBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MultiblockClientParticleMixin {

    @Inject(method = "levelEvent(Lnet/minecraft/world/entity/Entity;ILnet/minecraft/core/BlockPos;I)V", at = @At("HEAD"), cancellable = true)
    private void redirectFillerBreakParticles(@Nullable Entity source, int type, BlockPos pos, int data, CallbackInfo ci) {
        if (type != LevelEvent.PARTICLES_DESTROY_BLOCK) return;

        LevelAccessor level = (LevelAccessor)this;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof MultiblockFillerBlock) {
            ci.cancel();
        }
    }
}
