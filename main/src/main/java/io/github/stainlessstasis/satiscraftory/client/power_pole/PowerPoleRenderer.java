package io.github.stainlessstasis.satiscraftory.client.power_pole;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderer;
import io.github.stainlessstasis.manifold.multiblock.MultiblockShape;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.power_pole.PowerPoleBlock;
import io.github.stainlessstasis.satiscraftory.factory_component.power_pole.PowerPoleBlockEntity;
import io.github.stainlessstasis.satiscraftory.registry.SCBlockEntities;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class PowerPoleRenderer extends MultiblockRenderer<PowerPoleBlockEntity, MultiblockRenderState> {
    public static final Identifier TEXTURE = Satiscraftory.id("textures/block/power_pole.png");

    private final PowerPoleModel model;

    public PowerPoleRenderer(BlockEntityRendererProvider.Context context) {
        super(SCBlockEntities.POWER_POLE.get());
        this.model = new PowerPoleModel(context.bakeLayer(PowerPoleModel.LAYER_LOCATION));
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
