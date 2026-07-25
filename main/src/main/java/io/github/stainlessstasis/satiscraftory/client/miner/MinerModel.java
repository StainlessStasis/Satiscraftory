package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import io.github.stainlessstasis.satiscraftory.factory_component.miner.MinerAnimations;
import net.minecraft.client.animation.*;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.jspecify.annotations.NonNull;

public class MinerModel extends Model<MinerRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Satiscraftory.id("miner"), "main");
	private final KeyframeAnimation startup;
	private final KeyframeAnimation spinLoop;

	public MinerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
		this.startup = MinerAnimations.STARTUP.bake(root);
		this.spinLoop = MinerAnimations.SPIN_LOOP.bake(root);
	}

	@Override
	public void setupAnim(@NonNull MinerRenderState state) {
		super.setupAnim(state);
		if (state.startupAnimationState.isStarted()) {
			this.startup.apply(state.startupAnimationState, state.ageInTicks);
		}
		if (state.spinAnimationState.isStarted()) {
			this.spinLoop.apply(state.spinAnimationState, state.ageInTicks);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -6.0F));

		PartDefinition miner = root.addOrReplaceChild("miner", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition port_frame = miner.addOrReplaceChild("port_frame", CubeListBuilder.create().texOffs(214, 79).addBox(-2.005F, -20.0F, -2.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(286, 333).addBox(-18.0F, -20.0F, -2.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(354, 145).addBox(-14.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(354, 153).addBox(-14.0F, -20.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition port = miner.addOrReplaceChild("port", CubeListBuilder.create().texOffs(118, 103).addBox(-22.0F, -22.0F, 2.0F, 28.0F, 22.0F, 28.0F, new CubeDeformation(0.0F))
				.texOffs(128, 79).addBox(-19.0F, -24.0F, 9.0F, 22.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ore_chute = miner.addOrReplaceChild("ore_chute", CubeListBuilder.create().texOffs(0, 286).addBox(-21.0F, -21.0F, 30.0F, 26.0F, 20.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(148, 251).addBox(-18.0F, -23.0F, 30.0F, 20.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = ore_chute.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(148, 202).addBox(-17.0F, -31.5F, 24.0F, 0.0F, 14.0F, 35.0F, new CubeDeformation(0.0F))
				.texOffs(148, 153).addBox(1.0F, -31.5F, 24.0F, 0.0F, 14.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube2_r1 = ore_chute.addOrReplaceChild("cube2_r1", CubeListBuilder.create().texOffs(128, 43).addBox(-17.0F, -17.0F, 23.0F, 18.0F, 1.0F, 35.0F, new CubeDeformation(0.0F))
				.texOffs(218, 153).addBox(-17.0F, -32.0F, 23.0F, 18.0F, 1.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition frame = miner.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition beam_r1 = frame.addOrReplaceChild("beam_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-13.0F, -4.0F, -1.0F, 8.0F, 4.0F, 56.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -19.0F, 30.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition drill_hole = miner.addOrReplaceChild("drill_hole", CubeListBuilder.create().texOffs(200, 299).addBox(-21.0F, -6.0F, 59.0F, 26.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(66, 301).addBox(-21.0F, -6.0F, 83.0F, 26.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = drill_hole.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(308, 28).addBox(-24.0F, -6.0F, 11.0F, 22.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(306, 150).addBox(-24.0F, -6.0F, -13.0F, 22.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 59.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition base_frame_right = miner.addOrReplaceChild("base_frame_right", CubeListBuilder.create().texOffs(138, 319).addBox(8.0F, -0.25F, 5.0F, 4.0F, 2.0F, 19.0F, new CubeDeformation(0.0F))
				.texOffs(308, 53).addBox(8.0F, -5.75F, 27.25F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(318, 133).addBox(8.0F, -5.75F, 66.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(294, 180).addBox(5.0F, 1.25F, 57.35F, 4.0F, 1.0F, 27.0F, new CubeDeformation(0.0F))
				.texOffs(310, 111).addBox(8.0F, -0.25F, 42.6F, 4.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(332, 244).addBox(8.0F, -0.25F, 81.35F, 4.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = base_frame_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(80, 261).addBox(-1.0F, -3.0F, -1.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(198, 251).addBox(-1.0F, -3.0F, -15.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 21.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r4 = base_frame_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(356, 175).addBox(8.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 38.25F, 0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r5 = base_frame_right.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(356, 36).addBox(8.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 105.75F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r6 = base_frame_right.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(356, 26).addBox(8.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 67.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r7 = base_frame_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(256, 299).addBox(8.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.9163F, 0.0F, 0.0F));

		PartDefinition base_frame_left = miner.addOrReplaceChild("base_frame_left", CubeListBuilder.create().texOffs(320, 311).addBox(-12.0F, -0.25F, 5.0F, 4.0F, 2.0F, 19.0F, new CubeDeformation(0.0F))
				.texOffs(326, 12).addBox(-12.0F, -5.75F, 27.25F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(78, 326).addBox(-12.0F, -5.75F, 66.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(256, 311).addBox(-12.0F, -0.25F, 42.6F, 4.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(0, 336).addBox(-12.0F, -0.25F, 81.35F, 4.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(294, 208).addBox(-9.0F, 1.25F, 57.35F, 4.0F, 1.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition cube_r8 = base_frame_left.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(116, 261).addBox(-4.0F, -3.0F, -1.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(98, 261).addBox(-4.0F, -3.0F, 13.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 1.0F, 7.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r9 = base_frame_left.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(356, 215).addBox(-12.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 38.25F, 0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r10 = base_frame_left.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(356, 205).addBox(-12.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 105.75F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r11 = base_frame_left.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(356, 195).addBox(-12.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 67.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r12 = base_frame_left.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(356, 185).addBox(-12.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.9163F, 0.0F, 0.0F));

		PartDefinition base_frame = miner.addOrReplaceChild("base_frame", CubeListBuilder.create().texOffs(230, 144).addBox(-28.0F, -0.25F, 1.0F, 40.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(234, 68).addBox(-28.0F, -0.25F, 92.35F, 40.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(354, 161).addBox(0.0F, -2.25F, 91.35F, 4.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(104, 355).addBox(-20.0F, -2.25F, 91.35F, 4.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(246, 0).addBox(-25.0F, 0.25F, 84.35F, 34.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition big_ass_thing_in_the_back = miner.addOrReplaceChild("big_ass_thing_in_the_back", CubeListBuilder.create().texOffs(158, 0).addBox(-26.0F, -18.25F, 100.35F, 35.0F, 21.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(140, 296).addBox(-26.0F, -32.25F, 100.35F, 21.0F, 14.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(320, 332).addBox(-5.0F, -28.25F, 102.35F, 10.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_feet = miner.addOrReplaceChild("tower_feet", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = tower_feet.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot = right.addOrReplaceChild("tower_foot", CubeListBuilder.create().texOffs(340, 53).addBox(13.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(334, 0).addBox(6.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(78, 340).addBox(1.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot2 = right.addOrReplaceChild("tower_foot2", CubeListBuilder.create().texOffs(138, 340).addBox(13.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(320, 347).addBox(6.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(344, 70).addBox(1.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 33.0F));

		PartDefinition left = tower_feet.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition tower_foot3 = left.addOrReplaceChild("tower_foot3", CubeListBuilder.create().texOffs(344, 87).addBox(-18.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 349).addBox(-13.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(344, 257).addBox(-6.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot4 = left.addOrReplaceChild("tower_foot4", CubeListBuilder.create().texOffs(344, 274).addBox(-18.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(350, 133).addBox(-13.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(200, 347).addBox(-6.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 33.0F));

		PartDefinition tower = miner.addOrReplaceChild("tower", CubeListBuilder.create().texOffs(246, 12).addBox(-18.0F, -10.5F, 48.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(246, 20).addBox(-18.0F, -60.5F, 48.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(294, 236).addBox(-18.0F, -60.5F, 81.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 6.0F));

		PartDefinition cube_r13 = tower.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(148, 258).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -38.0F, 82.75F, 0.0F, -1.5708F, 0.5236F));

		PartDefinition cube_r14 = tower.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 227).addBox(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -53.85F, 81.75F, 0.0F, -1.5708F, -0.5236F));

		PartDefinition cube_r15 = tower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 217).addBox(-15.0F, -4.0F, -2.0F, 29.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -56.5F, 66.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r16 = tower.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(158, 30).addBox(-15.0F, -4.0F, -2.0F, 29.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, -56.5F, 66.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tower_legs_right = tower.addOrReplaceChild("tower_legs_right", CubeListBuilder.create().texOffs(46, 313).addBox(14.0F, -56.5F, 48.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(122, 301).addBox(14.0F, -56.5F, 81.0F, 4.0F, 50.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r17 = tower_legs_right.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(218, 180).addBox(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -53.85F, 52.75F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube_r18 = tower_legs_right.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(230, 79).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -38.0F, 53.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition tower_legs_left = tower.addOrReplaceChild("tower_legs_left", CubeListBuilder.create().texOffs(184, 319).addBox(-18.0F, -56.5F, 48.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(304, 311).addBox(-18.0F, -56.5F, 81.0F, 4.0F, 50.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r19 = tower_legs_left.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(218, 219).addBox(-1.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -53.85F, 52.75F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube_r20 = tower_legs_left.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(234, 30).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, -38.0F, 53.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition catwalk = miner.addOrReplaceChild("catwalk", CubeListBuilder.create().texOffs(62, 326).addBox(10.75F, -63.25F, 44.0F, 1.0F, 31.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(238, 324).addBox(9.75F, -37.25F, 44.0F, 1.0F, 34.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(284, 292).addBox(-9.0F, -62.5F, 34.0F, 20.0F, 2.0F, 17.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-27.0F, -62.5F, 51.0F, 38.0F, 2.0F, 41.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition lights = catwalk.addOrReplaceChild("lights", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition light = lights.addOrReplaceChild("light", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r21 = light.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 225).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(76, 227).addBox(2.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(0, 225).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -56.0F, 75.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition light3 = lights.addOrReplaceChild("light3", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition cube_r22 = light3.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(60, 225).addBox(-2.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(284, 258).addBox(-3.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(48, 225).addBox(-2.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -56.0F, 75.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light2 = lights.addOrReplaceChild("light2", CubeListBuilder.create(), PartPose.offsetAndRotation(53.0F, 0.0F, 81.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r23 = light2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(36, 225).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(80, 267).addBox(2.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(24, 225).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -56.0F, 75.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition light4 = lights.addOrReplaceChild("light4", CubeListBuilder.create(), PartPose.offsetAndRotation(73.0F, 0.0F, 45.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r24 = light4.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(174, 38).addBox(-2.0F, -2.0F, 0.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(200, 324).addBox(-3.0F, -4.0F, -13.0F, 1.0F, 5.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(94, 150).addBox(-2.0F, -2.0F, -8.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -56.5F, 75.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light5 = lights.addOrReplaceChild("light5", CubeListBuilder.create(), PartPose.offsetAndRotation(52.0F, -0.75F, 61.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r25 = light5.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(164, 340).addBox(-1.3244F, -1.7373F, -2.0F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(174, 40).addBox(-0.3244F, 0.2627F, 2.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -56.0F, 67.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light6 = lights.addOrReplaceChild("light6", CubeListBuilder.create(), PartPose.offset(1.0F, -0.75F, -33.0F));

		PartDefinition cube_r26 = light6.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(164, 354).addBox(-3.0F, -2.0F, 5.0F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(74, 150).addBox(-2.0F, 0.0F, 9.0F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -57.0F, 66.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition top = miner.addOrReplaceChild("top", CubeListBuilder.create().texOffs(74, 153).addBox(-15.0F, -120.0F, 64.0F, 14.0F, 57.0F, 17.0F, new CubeDeformation(0.0F))
				.texOffs(304, 74).addBox(-18.0F, -122.0F, 76.0F, 6.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 150).addBox(-12.0F, -122.0F, 76.0F, 6.0F, 36.0F, 31.0F, new CubeDeformation(0.0F))
				.texOffs(136, 153).addBox(-1.0F, -122.0F, 96.0F, 4.0F, 112.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 103).addBox(-17.0F, -126.0F, 61.0F, 18.0F, 6.0F, 41.0F, new CubeDeformation(0.0F))
				.texOffs(254, 333).addBox(0.0F, -142.0F, 68.5F, 4.0F, 24.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(230, 117).addBox(-15.0F, -125.0F, 84.0F, 19.0F, 6.0F, 21.0F, new CubeDeformation(0.0F))
				.texOffs(350, 350).addBox(-3.0F, -113.0F, 94.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition power_indicator = root.addOrReplaceChild("power_indicator", CubeListBuilder.create().texOffs(110, 326).addBox(-10.0F, -46.0F, 4.0F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cable_connection_point = power_indicator.addOrReplaceChild("cable_connection_point", CubeListBuilder.create(), PartPose.offset(-8.5F, -39.0F, 5.5F));

		PartDefinition drill = root.addOrReplaceChild("drill", CubeListBuilder.create().texOffs(222, 258).addBox(-15.0F, -50.0F, 64.0F, 15.0F, 25.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(0, 313).addBox(-13.0F, -61.0F, 66.0F, 11.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(294, 244).addBox(-12.0F, -25.0F, 67.0F, 9.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(350, 332).addBox(-12.0F, -39.0F, 80.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition drill_head = drill.addOrReplaceChild("drill_head", CubeListBuilder.create().texOffs(0, 266).addBox(-10.0F, -20.9688F, -10.0F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 72.0F));

		PartDefinition drill_ring = drill_head.addOrReplaceChild("drill_ring", CubeListBuilder.create().texOffs(356, 225).addBox(-10.0F, -10.0F, -10.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(358, 115).addBox(8.0F, -10.0F, -10.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 6.0F));

		PartDefinition cube_r27 = drill_ring.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(320, 359).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r28 = drill_ring.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(358, 291).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r29 = drill_ring.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(358, 104).addBox(-17.5F, -2.1F, 5.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(358, 12).addBox(-0.5F, -2.1F, 5.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -8.0F, -5.25F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r30 = drill_ring.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(130, 357).addBox(16.5F, -2.1F, 4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(78, 357).addBox(-0.5F, -2.1F, 4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -8.0F, -5.25F, 0.0F, 0.7854F, 0.0F));

		PartDefinition drill_ring_2 = drill_head.addOrReplaceChild("drill_ring_2", CubeListBuilder.create().texOffs(286, 357).addBox(7.0F, -38.0F, 15.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(54, 364).addBox(-9.0F, -38.0F, 15.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -19.0F));

		PartDefinition cube_r31 = drill_ring_2.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(364, 0).addBox(-18.0F, -2.0F, -12.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(36, 363).addBox(-34.0F, -2.0F, -12.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r32 = drill_ring_2.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(362, 244).addBox(-4.75F, -2.1F, -27.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(254, 361).addBox(-19.75F, -2.1F, -27.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r33 = drill_ring_2.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(18, 361).addBox(-32.75F, -2.1F, 7.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 361).addBox(-17.5F, -2.1F, 7.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition drill_ring_3 = drill_head.addOrReplaceChild("drill_ring_3", CubeListBuilder.create().texOffs(30, 336).addBox(7.0F, -38.0F, 22.5F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(30, 345).addBox(-7.0F, -38.0F, 22.5F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 23.0F, -25.5F));

		PartDefinition cube_r34 = drill_ring_3.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(358, 364).addBox(-29.75F, -2.1F, -17.25F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(340, 364).addBox(-17.0F, -2.1F, -17.25F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r35 = drill_ring_3.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(218, 364).addBox(-21.0F, -2.1F, 18.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(200, 364).addBox(-8.5F, -2.1F, 18.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r36 = drill_ring_3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(236, 365).addBox(-33.5F, -2.0F, 3.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(358, 302).addBox(-19.5F, -2.0F, 3.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition drill_ring_4 = drill_head.addOrReplaceChild("drill_ring_4", CubeListBuilder.create().texOffs(66, 286).addBox(7.0F, -37.0F, 23.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(66, 293).addBox(-3.0F, -37.0F, 23.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 25.0F, -26.0F));

		PartDefinition cube_r37 = drill_ring_4.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(358, 126).addBox(-10.5F, -1.1F, -25.25F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(150, 357).addBox(-19.75F, -1.1F, -25.25F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r38 = drill_ring_4.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(356, 46).addBox(-28.25F, -1.1F, 11.75F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(30, 354).addBox(-19.0F, -1.1F, 11.75F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r39 = drill_ring_4.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(344, 104).addBox(-32.0F, -1.0F, -8.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(322, 67).addBox(-22.0F, -1.0F, -8.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition drill_ring_5 = drill_head.addOrReplaceChild("drill_ring_5", CubeListBuilder.create().texOffs(226, 347).addBox(-2.0F, -1.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(226, 353).addBox(-8.0F, -1.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -9.0F, -26.0F));

		PartDefinition cube_r40 = drill_ring_5.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(150, 364).addBox(-2.0F, -1.0F, 22.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-28.5F, 0.0F, 28.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r41 = drill_ring_5.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(272, 361).addBox(-2.0F, -1.0F, 22.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-28.5F, 0.0F, 22.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r42 = drill_ring_5.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(222, 38).addBox(-7.1F, -1.0F, 23.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(192, 38).addBox(-1.25F, -1.0F, 23.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -0.1F, 6.5F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r43 = drill_ring_5.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(212, 38).addBox(-28.35F, -1.0F, -4.75F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(202, 38).addBox(-22.5F, -1.0F, -4.75F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -0.1F, 6.5F, 0.0F, 2.3562F, 0.0F));

		PartDefinition drill_ring_6 = drill_head.addOrReplaceChild("drill_ring_6", CubeListBuilder.create().texOffs(158, 38).addBox(-10.0F, -8.5F, 70.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(66, 217).addBox(-9.0F, -7.5F, 71.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(270, 333).addBox(-10.0F, -44.75F, 70.0F, 4.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -72.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}
}