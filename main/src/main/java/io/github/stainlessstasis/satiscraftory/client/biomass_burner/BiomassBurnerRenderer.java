package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.biomass_burner.BiomassBurnerBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BiomassBurnerRenderer extends MultiblockRenderer<BiomassBurnerBlockEntity, MultiblockRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/biomass_burner.png");

    private final BiomassBurnerModel model;

    public BiomassBurnerRenderer(BlockEntityRendererProvider.Context context) {
        super(SCBlockEntities.BIOMASS_BURNER.get());
        this.model = new BiomassBurnerModel(context.bakeLayer(BiomassBurnerModel.LAYER_LOCATION));
    }

    @Override
    protected MultiblockShape shape() {
        return BiomassBurnerBlock.MULTIBLOCK_SHAPE;
    }

    @Override
    public Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public PoweredFactoryModel<MultiblockRenderState> getModel() {
        return model;
    }

    @Override
    public @NonNull MultiblockRenderState createRenderState() {
        return new MultiblockRenderState();
    }
}
