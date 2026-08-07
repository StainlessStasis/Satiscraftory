package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.factory_component.power_producer.PowerProducerBlockEntity;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.manifold.registry.ManifoldBlockEntities;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.client.power_pole.PowerPoleModel;
import io.github.stainlessstasis.satiscraftory.factory_component.power_pole.PowerPoleBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.power_pole.PowerPoleBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BiomassBurnerRenderer extends MultiblockRenderer<PowerProducerBlockEntity, MultiblockRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/biomass_burner.png");

    private final BiomassBurnerModel model;

    public BiomassBurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(ManifoldBlockEntities.POWER_PRODUCER.get());
        this.model = new BiomassBurnerModel(context.bakeLayer(BiomassBurnerModel.LAYER_LOCATION));
    }

    @Override
    protected MultiblockShape shape() {
        return PowerPoleBlock.MULTIBLOCK_SHAPE;
    }

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public Model<MultiblockRenderState> getModel() {
        return model;
    }

    @Override
    public @NonNull MultiblockRenderState createRenderState() {
        return new MultiblockRenderState();
    }
}
