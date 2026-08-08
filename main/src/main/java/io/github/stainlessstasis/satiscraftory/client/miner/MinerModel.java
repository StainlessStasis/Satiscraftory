package io.github.stainlessstasis.satiscraftory.client.miner;

import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
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
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MinerModel extends PoweredFactoryModel<MinerRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Satiscraftory.id("miner"), "main");
	private final ModelPart powerIndicator;
	private final List<ModelPart> powerIndicatorAncestry;
	private final KeyframeAnimation startupRotation;
	private final KeyframeAnimation startupDescend;
	private final KeyframeAnimation startupAlreadyDescended;
	private final KeyframeAnimation spinLoop;
	private final KeyframeAnimation cooldown;
	private final KeyframeAnimation idle;

	public MinerModel(ModelPart root) {
		super(root, RenderTypes::entityCutout);
		ModelPart rootPart = root.getChild("root");
		ModelPart powerPart = rootPart.getChild("power");
		this.powerIndicatorAncestry = List.of(rootPart, powerPart);
		this.powerIndicator = rootPart.getChild("power").getChild("indicator");
		this.powerIndicator.visible = false;

		this.startupRotation = MinerAnimations.STARTUP_ROTATION.bake(root);
		this.startupDescend = MinerAnimations.STARTUP_DESCEND.bake(root);
		this.startupAlreadyDescended = MinerAnimations.STARTUP_ALREADY_DESCENDED.bake(root);
		this.spinLoop = MinerAnimations.SPIN_LOOP.bake(root);
		this.cooldown = MinerAnimations.COOLDOWN.bake(root);
		this.idle = MinerAnimations.IDLE.bake(root);
	}

	@Override
	public ModelPart getPowerIndicatorPart() {
		return powerIndicator;
	}

	@Override
	public List<ModelPart> getPowerIndicatorAncestry() {
		return powerIndicatorAncestry;
	}

	@Override
	public void setupAnim(@NonNull MinerRenderState state) {
		super.setupAnim(state);
		if (state.startupRotationState.isStarted()) {
			this.startupRotation.apply(state.startupRotationState, state.ageInTicks);
		}
		if (state.startupDescendState.isStarted()) {
			this.startupDescend.apply(state.startupDescendState, state.ageInTicks);
		}
		if (state.startupAlreadyDescendedState.isStarted()) {
			this.startupAlreadyDescended.apply(state.startupAlreadyDescendedState, state.ageInTicks);
		}
		if (state.spinAnimationState.isStarted()) {
			this.spinLoop.apply(state.spinAnimationState, state.ageInTicks);
		}
		if (state.cooldownAnimationState.isStarted()) {
			this.cooldown.apply(state.cooldownAnimationState, state.ageInTicks);
		}
		if (state.idleAnimationState.isStarted()) {
			this.idle.apply(state.idleAnimationState, state.ageInTicks);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -6.0F));

		PartDefinition miner = root.addOrReplaceChild("miner", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition port_frame = miner.addOrReplaceChild("port_frame", CubeListBuilder.create().texOffs(214, 79).addBox(-2.005F, -20.0F, -2.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(174, 347).addBox(-18.0F, -20.0F, -2.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(252, 299).addBox(-14.0F, -4.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(356, 167).addBox(-14.0F, -20.0F, -2.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition port = miner.addOrReplaceChild("port", CubeListBuilder.create().texOffs(118, 103).addBox(-22.0F, -22.0F, 2.0F, 28.0F, 22.0F, 28.0F, new CubeDeformation(0.0F))
				.texOffs(128, 79).addBox(-19.0F, -24.0F, 9.0F, 22.0F, 2.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ore_chute = miner.addOrReplaceChild("ore_chute", CubeListBuilder.create().texOffs(106, 296).addBox(-21.0F, -21.0F, 30.0F, 26.0F, 20.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(150, 251).addBox(-18.0F, -23.0F, 30.0F, 20.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = ore_chute.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(150, 202).addBox(-17.0F, -31.5F, 24.0F, 0.0F, 14.0F, 35.0F, new CubeDeformation(0.0F))
				.texOffs(150, 153).addBox(1.0F, -31.5F, 24.0F, 0.0F, 14.0F, 35.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube2_r1 = ore_chute.addOrReplaceChild("cube2_r1", CubeListBuilder.create().texOffs(128, 43).addBox(-17.0F, -17.0F, 23.0F, 18.0F, 1.0F, 35.0F, new CubeDeformation(0.0F))
				.texOffs(220, 153).addBox(-17.0F, -32.0F, 23.0F, 18.0F, 1.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.5236F, 0.0F, 0.0F));

		PartDefinition frame = miner.addOrReplaceChild("frame", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition beam_r1 = frame.addOrReplaceChild("beam_r1", CubeListBuilder.create().texOffs(0, 43).addBox(-13.0F, -4.0F, -1.0F, 8.0F, 4.0F, 56.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -19.0F, 30.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition drill_hole = miner.addOrReplaceChild("drill_hole", CubeListBuilder.create().texOffs(172, 307).addBox(-21.0F, -6.0F, 59.0F, 26.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(228, 307).addBox(-21.0F, -6.0F, 83.0F, 26.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = drill_hole.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(308, 150).addBox(-24.0F, -6.0F, 11.0F, 22.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(308, 28).addBox(-24.0F, -6.0F, -13.0F, 22.0F, 23.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 59.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition base_frame_right = miner.addOrReplaceChild("base_frame_right", CubeListBuilder.create().texOffs(322, 53).addBox(8.0F, -0.25F, 5.0F, 4.0F, 2.0F, 19.0F, new CubeDeformation(0.0F))
				.texOffs(196, 332).addBox(8.0F, -5.75F, 27.25F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(228, 332).addBox(8.0F, -5.75F, 66.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(296, 199).addBox(5.0F, 1.25F, 57.35F, 4.0F, 1.0F, 27.0F, new CubeDeformation(0.0F))
				.texOffs(310, 105).addBox(8.0F, -0.25F, 42.6F, 4.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(118, 344).addBox(8.0F, -0.25F, 81.35F, 4.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = base_frame_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(106, 288).addBox(-1.0F, -3.0F, -1.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(200, 251).addBox(-1.0F, -3.0F, -15.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 1.0F, 21.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r4 = base_frame_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(358, 233).addBox(8.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 38.25F, 0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r5 = base_frame_right.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(358, 223).addBox(8.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 105.75F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r6 = base_frame_right.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(358, 213).addBox(8.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 67.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r7 = base_frame_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(46, 217).addBox(8.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.9163F, 0.0F, 0.0F));

		PartDefinition base_frame_left = miner.addOrReplaceChild("base_frame_left", CubeListBuilder.create().texOffs(118, 323).addBox(-12.0F, -0.25F, 5.0F, 4.0F, 2.0F, 19.0F, new CubeDeformation(0.0F))
				.texOffs(338, 326).addBox(-12.0F, -5.75F, 27.25F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(338, 340).addBox(-12.0F, -5.75F, 66.0F, 4.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(318, 127).addBox(-12.0F, -0.25F, 42.6F, 4.0F, 2.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(72, 346).addBox(-12.0F, -0.25F, 81.35F, 4.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(296, 227).addBox(-9.0F, 1.25F, 57.35F, 4.0F, 1.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition cube_r8 = base_frame_left.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(366, 141).addBox(-4.0F, -3.0F, -1.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(130, 357).addBox(-4.0F, -3.0F, 13.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 1.0F, 7.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r9 = base_frame_left.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(214, 363).addBox(-12.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 38.25F, 0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r10 = base_frame_left.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(190, 363).addBox(-12.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 105.75F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r11 = base_frame_left.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(72, 359).addBox(-12.005F, 18.5F, -21.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 67.0F, -0.9163F, 0.0F, 0.0F));

		PartDefinition cube_r12 = base_frame_left.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(358, 243).addBox(-12.005F, 18.5F, 13.5F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.9163F, 0.0F, 0.0F));

		PartDefinition base_frame = miner.addOrReplaceChild("base_frame", CubeListBuilder.create().texOffs(230, 144).addBox(-28.0F, -0.25F, 1.0F, 40.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(234, 68).addBox(-28.0F, -0.25F, 92.35F, 40.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(358, 105).addBox(0.0F, -2.25F, 91.35F, 4.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(358, 199).addBox(-20.0F, -2.25F, 91.35F, 4.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(246, 0).addBox(-25.0F, 0.25F, 84.35F, 34.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition big_ass_thing_in_the_back = miner.addOrReplaceChild("big_ass_thing_in_the_back", CubeListBuilder.create().texOffs(158, 0).addBox(-26.0F, -18.25F, 100.35F, 35.0F, 21.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(304, 74).addBox(-26.0F, -32.25F, 100.35F, 21.0F, 14.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(164, 332).addBox(-5.1F, -28.15F, 102.35F, 11.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_feet = miner.addOrReplaceChild("tower_feet", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = tower_feet.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot = right.addOrReplaceChild("tower_foot", CubeListBuilder.create().texOffs(196, 346).addBox(13.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(334, 0).addBox(6.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(222, 346).addBox(1.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot2 = right.addOrReplaceChild("tower_foot2", CubeListBuilder.create().texOffs(346, 255).addBox(13.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(338, 354).addBox(6.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(346, 272).addBox(1.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 33.0F));

		PartDefinition left = tower_feet.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition tower_foot3 = left.addOrReplaceChild("tower_foot3", CubeListBuilder.create().texOffs(346, 289).addBox(-18.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(356, 26).addBox(-13.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(346, 306).addBox(-6.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tower_foot4 = left.addOrReplaceChild("tower_foot4", CubeListBuilder.create().texOffs(148, 347).addBox(-18.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(356, 38).addBox(-13.0F, -6.5F, 52.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(276, 349).addBox(-6.0F, -6.5F, 52.0F, 5.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 33.0F));

		PartDefinition tower = miner.addOrReplaceChild("tower", CubeListBuilder.create().texOffs(246, 12).addBox(-18.0F, -10.5F, 48.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(246, 20).addBox(-18.0F, -60.5F, 48.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(172, 299).addBox(-18.0F, -60.5F, 81.0F, 36.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 6.0F));

		PartDefinition cube_r13 = tower.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(150, 258).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -38.0F, 82.75F, 0.0F, -1.5708F, 0.5236F));

		PartDefinition cube_r14 = tower.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(46, 229).addBox(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -53.85F, 81.75F, 0.0F, -1.5708F, -0.5236F));

		PartDefinition cube_r15 = tower.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(304, 97).addBox(-15.0F, -4.0F, -2.0F, 29.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -56.5F, 66.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r16 = tower.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(158, 30).addBox(-15.0F, -4.0F, -2.0F, 29.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, -56.5F, 66.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition tower_legs_right = tower.addOrReplaceChild("tower_legs_right", CubeListBuilder.create().texOffs(0, 326).addBox(14.0F, -56.5F, 48.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(40, 322).addBox(14.0F, -56.5F, 81.0F, 4.0F, 50.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r17 = tower_legs_right.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(220, 180).addBox(0.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, -53.85F, 52.75F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube_r18 = tower_legs_right.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(230, 79).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.0F, -38.0F, 53.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition tower_legs_left = tower.addOrReplaceChild("tower_legs_left", CubeListBuilder.create().texOffs(16, 326).addBox(-18.0F, -56.5F, 48.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(56, 322).addBox(-18.0F, -56.5F, 81.0F, 4.0F, 50.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r19 = tower_legs_left.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(220, 219).addBox(-1.0F, -2.0F, -2.0F, 1.0F, 2.0F, 37.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, -53.85F, 52.75F, -0.5236F, 0.0F, 0.0F));

		PartDefinition cube_r20 = tower_legs_left.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(234, 30).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 36.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-16.0F, -38.0F, 53.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition catwalk = miner.addOrReplaceChild("catwalk", CubeListBuilder.create().texOffs(122, 229).addBox(10.75F, -63.25F, 44.0F, 1.0F, 31.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(322, 326).addBox(9.75F, -37.25F, 44.0F, 1.0F, 34.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(296, 180).addBox(-9.0F, -62.5F, 34.0F, 20.0F, 2.0F, 17.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-27.0F, -62.5F, 51.0F, 38.0F, 2.0F, 41.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition lights = catwalk.addOrReplaceChild("lights", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition light = lights.addOrReplaceChild("light", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r21 = light.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(58, 227).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(286, 258).addBox(2.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(46, 227).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -56.0F, 75.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition light3 = lights.addOrReplaceChild("light3", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition cube_r22 = light3.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(242, 150).addBox(-2.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(286, 292).addBox(-3.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(234, 76).addBox(-2.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -56.0F, 75.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light2 = lights.addOrReplaceChild("light2", CubeListBuilder.create(), PartPose.offsetAndRotation(53.0F, 0.0F, 81.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r23 = light2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(234, 74).addBox(-3.0F, -2.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(46, 288).addBox(2.0F, -4.0F, -18.0F, 1.0F, 5.0F, 29.0F, new CubeDeformation(0.0F))
				.texOffs(230, 150).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.0F, -56.0F, 75.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition light4 = lights.addOrReplaceChild("light4", CubeListBuilder.create(), PartPose.offsetAndRotation(73.0F, 0.0F, 45.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r24 = light4.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(174, 38).addBox(-2.0F, -2.0F, 0.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(284, 326).addBox(-3.0F, -4.0F, -13.0F, 1.0F, 5.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(94, 150).addBox(-2.0F, -2.0F, -8.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, -56.5F, 75.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light5 = lights.addOrReplaceChild("light5", CubeListBuilder.create(), PartPose.offsetAndRotation(52.0F, -0.75F, 61.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r25 = light5.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(364, 0).addBox(-1.3244F, -1.7373F, -2.0F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(174, 40).addBox(-0.3244F, 0.2627F, 2.0F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, -56.0F, 67.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition light6 = lights.addOrReplaceChild("light6", CubeListBuilder.create(), PartPose.offset(1.0F, -0.75F, -33.0F));

		PartDefinition cube_r26 = light6.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(364, 74).addBox(-3.0F, -2.0F, 5.0F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(74, 150).addBox(-2.0F, 0.0F, 9.0F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -57.0F, 66.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition top = miner.addOrReplaceChild("top", CubeListBuilder.create().texOffs(74, 153).addBox(-16.0F, -120.0F, 64.0F, 15.0F, 59.0F, 17.0F, new CubeDeformation(0.0F))
				.texOffs(0, 217).addBox(-13.0F, -120.0F, 66.0F, 10.0F, 59.0F, 13.0F, new CubeDeformation(0.0F))
				.texOffs(0, 289).addBox(-18.0F, -122.0F, 76.0F, 6.0F, 23.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 150).addBox(-12.0F, -122.0F, 76.0F, 6.0F, 36.0F, 31.0F, new CubeDeformation(0.0F))
				.texOffs(138, 153).addBox(-1.0F, -122.0F, 96.0F, 4.0F, 112.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 103).addBox(-17.0F, -126.0F, 61.0F, 18.0F, 6.0F, 41.0F, new CubeDeformation(0.0F))
				.texOffs(126, 267).addBox(0.0F, -142.0F, 68.5F, 4.0F, 24.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(230, 117).addBox(-15.0F, -125.0F, 84.0F, 19.0F, 6.0F, 21.0F, new CubeDeformation(0.0F))
				.texOffs(102, 357).addBox(-3.0F, -113.0F, 94.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition power = root.addOrReplaceChild("power", CubeListBuilder.create().texOffs(302, 349).addBox(-9.5F, -46.0F, 4.0F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition indicator = power.addOrReplaceChild("indicator", CubeListBuilder.create().texOffs(248, 346).addBox(-9.5F, -46.0F, 4.0F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition drill = root.addOrReplaceChild("drill", CubeListBuilder.create().texOffs(224, 258).addBox(-15.0F, -50.0F, 64.0F, 15.0F, 25.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(72, 323).addBox(-13.0F, -61.0F, 66.0F, 11.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(326, 12).addBox(-12.0F, -25.0F, 67.0F, 9.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(356, 149).addBox(-12.0F, -39.0F, 80.0F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition drill_head = drill.addOrReplaceChild("drill_head", CubeListBuilder.create().texOffs(46, 268).addBox(-10.0F, -20.9688F, -10.0F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 72.0F));

		PartDefinition drill_ring = drill_head.addOrReplaceChild("drill_ring", CubeListBuilder.create().texOffs(364, 14).addBox(-10.0F, -10.0F, -10.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(260, 366).addBox(8.0F, -10.0F, -10.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 6.0F));

		PartDefinition cube_r27 = drill_ring.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(338, 366).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -15.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r28 = drill_ring.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(280, 366).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, 3.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r29 = drill_ring.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(366, 130).addBox(-17.5F, -2.1F, 5.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(366, 119).addBox(-0.5F, -2.1F, 5.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -8.0F, -5.25F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r30 = drill_ring.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(150, 364).addBox(16.5F, -2.1F, 4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(130, 364).addBox(-0.5F, -2.1F, 4.0F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.0F, -8.0F, -5.25F, 0.0F, 0.7854F, 0.0F));

		PartDefinition drill_ring_2 = drill_head.addOrReplaceChild("drill_ring_2", CubeListBuilder.create().texOffs(358, 366).addBox(7.0F, -38.0F, 15.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(370, 175).addBox(-9.0F, -38.0F, 15.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, -19.0F));

		PartDefinition cube_r31 = drill_ring_2.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(370, 88).addBox(-18.0F, -2.0F, -12.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(72, 369).addBox(-34.0F, -2.0F, -12.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r32 = drill_ring_2.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(368, 354).addBox(-4.75F, -2.1F, -27.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(368, 60).addBox(-19.75F, -2.1F, -27.5F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r33 = drill_ring_2.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(368, 50).addBox(-32.75F, -2.1F, 7.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(314, 367).addBox(-17.5F, -2.1F, 7.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, -6.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition drill_ring_3 = drill_head.addOrReplaceChild("drill_ring_3", CubeListBuilder.create().texOffs(102, 346).addBox(7.0F, -38.0F, 22.5F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(90, 371).addBox(-7.0F, -38.0F, 22.5F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 23.0F, -25.5F));

		PartDefinition cube_r34 = drill_ring_3.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(370, 343).addBox(-29.75F, -2.1F, -17.25F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(370, 333).addBox(-17.0F, -2.1F, -17.25F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r35 = drill_ring_3.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(370, 323).addBox(-21.0F, -2.1F, 18.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(370, 185).addBox(-8.5F, -2.1F, 18.75F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r36 = drill_ring_3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(170, 371).addBox(-33.5F, -2.0F, 3.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(106, 371).addBox(-19.5F, -2.0F, 3.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, -36.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition drill_ring_4 = drill_head.addOrReplaceChild("drill_ring_4", CubeListBuilder.create().texOffs(308, 53).addBox(7.0F, -37.0F, 23.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(308, 60).addBox(-3.0F, -37.0F, 23.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 25.0F, -26.0F));

		PartDefinition cube_r37 = drill_ring_4.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(372, 281).addBox(-10.5F, -1.1F, -25.25F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(372, 274).addBox(-19.75F, -1.1F, -25.25F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r38 = drill_ring_4.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(372, 267).addBox(-28.25F, -1.1F, 11.75F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(372, 260).addBox(-19.0F, -1.1F, 11.75F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r39 = drill_ring_4.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(372, 253).addBox(-32.0F, -1.0F, -8.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(370, 98).addBox(-22.0F, -1.0F, -8.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -36.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition drill_ring_5 = drill_head.addOrReplaceChild("drill_ring_5", CubeListBuilder.create().texOffs(260, 360).addBox(-2.0F, -1.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(372, 288).addBox(-8.0F, -1.0F, 24.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -9.0F, -26.0F));

		PartDefinition cube_r40 = drill_ring_5.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(372, 300).addBox(-2.0F, -1.0F, 22.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-28.5F, 0.0F, 28.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r41 = drill_ring_5.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(372, 294).addBox(-2.0F, -1.0F, 22.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-28.5F, 0.0F, 22.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r42 = drill_ring_5.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(222, 38).addBox(-7.1F, -1.0F, 23.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(192, 38).addBox(-1.25F, -1.0F, 23.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -0.1F, 6.5F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r43 = drill_ring_5.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(212, 38).addBox(-28.35F, -1.0F, -4.75F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(202, 38).addBox(-22.5F, -1.0F, -4.75F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -0.1F, 6.5F, 0.0F, 2.3562F, 0.0F));

		PartDefinition drill_ring_6 = drill_head.addOrReplaceChild("drill_ring_6", CubeListBuilder.create().texOffs(158, 38).addBox(-10.0F, -8.5F, 70.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(224, 30).addBox(-9.0F, -7.5F, 71.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(260, 332).addBox(-10.0F, -44.75F, 70.0F, 4.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -72.0F));

		return LayerDefinition.create(meshdefinition, 512, 512);
	}
}