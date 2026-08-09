package io.github.stainlessstasis.satiscraftory.client.power_pole;

import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class PowerPoleModel extends Model<MultiblockRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Satiscraftory.id("power_pole"), "main");

	public PowerPoleModel(ModelPart root) {
		super(root, RenderTypes::entitySolid);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(36, 29).addBox(-2.5F, -6.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 36).addBox(-2.5F, -22.0F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-2.5F, -57.0F, -2.5F, 5.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 15).addBox(-3.5F, -58.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(-2.0F, -59.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(36, 36).addBox(-2.0F, -49.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(36, 17).addBox(-2.0F, -30.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 23).addBox(-2.0F, -19.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(28, 0).addBox(-1.5F, -44.0F, -1.5F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(40, 5).addBox(2.0F, -17.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
}