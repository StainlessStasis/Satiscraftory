package io.github.stainlessstasis.manifold.client.factory_power;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class PoweredFactoryModel<S extends MultiblockRenderState> extends Model<S> {
    protected PoweredFactoryModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
    }

    public abstract ModelPart getPowerIndicatorPart();
    public @Nullable ModelPart getPowerIndicatorParent() {
        return null;
    }
}