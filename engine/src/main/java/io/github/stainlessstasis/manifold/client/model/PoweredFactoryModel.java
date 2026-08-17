package io.github.stainlessstasis.manifold.client.model;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Function;

public abstract class PoweredFactoryModel<S extends MultiblockRenderState> extends Model<S> implements HorizontallyCenteredModel {
    protected PoweredFactoryModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
    }

    public abstract ModelPart getPowerIndicatorPart();
    public abstract List<ModelPart> getPowerIndicatorAncestry();
}