package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MinerModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Satiscraftory.id("miner"), "main");

	private final ModelPart root;
	private final ModelPart portFrame;
	private final ModelPart port;
	private final ModelPart wip;

	public MinerModel(ModelPart root) {
		this.root = root.getChild("root");
		this.portFrame = this.root.getChild("port_frame");
		this.port = this.root.getChild("port");
		this.wip = this.root.getChild("WIP");
	}

	public ModelPart root() {
		return root;
	}

	public ModelPart portFrame() {
		return portFrame;
	}

	public ModelPart port() {
		return port;
	}

	public ModelPart wip() {
		return wip;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -6.0F));

		PartDefinition port_frame = root.addOrReplaceChild("port_frame", CubeListBuilder.create().texOffs(128, 144).addBox(-2.005F, -20.0F, -2.0F, 4.005F, 20.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(116, 144).addBox(-18.0F, -20.0F, -2.0F, 4.005F, 20.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(140, 144).addBox(-14.0F, -4.0F, -2.0F, 12.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(140, 150).addBox(-14.0F, -20.0F, -2.0F, 12.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition port = root.addOrReplaceChild("port", CubeListBuilder.create().texOffs(0, 144).addBox(-22.0F, -24.0F, 0.0F, 28.0F, 24.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition WIP = root.addOrReplaceChild("WIP", CubeListBuilder.create().texOffs(0, 0).addBox(-32.0F, -80.0F, 30.0F, 48.0F, 80.0F, 64.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}
}