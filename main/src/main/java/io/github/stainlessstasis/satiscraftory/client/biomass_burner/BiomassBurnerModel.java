package io.github.stainlessstasis.satiscraftory.client.biomass_burner;

import io.github.stainlessstasis.manifold.client.factory_power.PoweredFactoryModel;
import io.github.stainlessstasis.manifold.client.multiblock.MultiblockRenderState;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class BiomassBurnerModel extends PoweredFactoryModel<MultiblockRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Satiscraftory.id("biomass_burner"), "main");

    public BiomassBurnerModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 24.0F));

        PartDefinition burner = root.addOrReplaceChild("burner", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -5.0F));

        PartDefinition port_frame = burner.addOrReplaceChild("port_frame", CubeListBuilder.create().texOffs(60, 214).addBox(-2.005F, -20.0F, 3.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(76, 214).addBox(-18.0F, -20.0F, 3.0F, 4.005F, 20.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(212, 172).addBox(-14.0F, -4.0F, 3.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(212, 180).addBox(-14.0F, -20.0F, 3.0F, 12.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -30.0F));

        PartDefinition port = burner.addOrReplaceChild("port", CubeListBuilder.create().texOffs(34, 151).addBox(6.0F, -21.0F, -25.0F, 5.0F, 21.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(74, 184).addBox(-10.995F, -21.0F, -25.0F, 5.0F, 21.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(182, 194).addBox(-6.0F, 0.0F, -25.0F, 12.0F, 0.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(140, 194).addBox(-6.0F, -21.0F, -25.0F, 12.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(218, 140).addBox(-6.0F, -16.0F, -25.0F, 12.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

        PartDefinition walls = burner.addOrReplaceChild("walls", CubeListBuilder.create().texOffs(0, 151).addBox(-7.0F, -39.0F, -21.0F, 14.0F, 38.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(96, 142).addBox(-7.0F, -39.0F, 0.25F, 14.0F, 39.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = walls.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(148, 15).addBox(-1.25F, -39.001F, 6.1813F, 14.0F, 39.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -23.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition cube_r2 = walls.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(130, 142).addBox(11.75F, -39.002F, -7.9688F, 14.0F, 39.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -23.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition cube_r3 = walls.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(92, 0).addBox(-25.75F, -42.002F, -13.9688F, 14.0F, 42.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, 0.0F, -23.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition cube_r4 = walls.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(62, 142).addBox(-12.75F, -39.001F, 6.1813F, 14.0F, 39.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, 0.0F, -23.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition end_caps = burner.addOrReplaceChild("end_caps", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -24.0F));

        PartDefinition top = end_caps.addOrReplaceChild("top", CubeListBuilder.create().texOffs(62, 99).addBox(-4.5564F, -45.0F, 4.0F, 9.1127F, 6.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(62, 127).addBox(-11.0F, -44.996F, 10.4436F, 22.0F, 6.0F, 9.1127F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition octagon_r1 = top.addOrReplaceChild("octagon_r1", CubeListBuilder.create().texOffs(86, 51).addBox(-11.0F, -4.998F, -4.5564F, 22.0F, 6.0F, 9.1127F, new CubeDeformation(0.0F))
                .texOffs(0, 95).addBox(-4.5564F, -4.994F, -11.0F, 9.1127F, 6.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -40.0F, 15.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bottom = end_caps.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(124, 99).addBox(-4.5564F, -45.0F, 4.0F, 9.1127F, 6.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(138, 0).addBox(-11.0F, -44.996F, 10.4436F, 22.0F, 6.0F, 9.1127F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 39.0F, 0.0F));

        PartDefinition octagon_r2 = bottom.addOrReplaceChild("octagon_r2", CubeListBuilder.create().texOffs(124, 127).addBox(-11.0F, -4.998F, -4.5564F, 22.0F, 6.0F, 9.1127F, new CubeDeformation(0.0F))
                .texOffs(0, 123).addBox(-4.5564F, -4.994F, -11.0F, 9.1127F, 6.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -40.0F, 15.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition foundation = burner.addOrReplaceChild("foundation", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r5 = foundation.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 32).addBox(-29.0F, -2.999F, -20.0F, 9.0F, 2.0F, 34.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-20.0F, -2.995F, -15.0F, 17.0F, 3.0F, 29.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r6 = foundation.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(80, 68).addBox(-31.85F, -2.995F, -13.9F, 9.0F, 2.0F, 29.0F, new CubeDeformation(0.0F))
                .texOffs(0, 68).addBox(-23.0F, -2.999F, -13.9187F, 16.0F, 3.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition control_panel = burner.addOrReplaceChild("control_panel", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r7 = control_panel.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(226, 208).addBox(-20.3F, -9.0F, -1.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(140, 184).addBox(-22.7F, -12.0F, -2.0F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(156, 76).addBox(-26.7F, -13.0F, -13.0F, 13.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition ladder = burner.addOrReplaceChild("ladder", CubeListBuilder.create(), PartPose.offset(-29.0F, 0.0F, -58.0F));

        PartDefinition ladder_support_r1 = ladder.addOrReplaceChild("ladder_support_r1", CubeListBuilder.create().texOffs(16, 202).addBox(-8.0F, -63.0F, -8.2F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 192).addBox(-6.0F, -60.0F, -8.2F, 1.0F, 28.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.75F, 20.75F, 49.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition grinder = burner.addOrReplaceChild("grinder", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition death_and_destruction = grinder.addOrReplaceChild("death_and_destruction", CubeListBuilder.create().texOffs(226, 222).addBox(5.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 227).addBox(2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(104, 227).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(116, 227).addBox(-4.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(42, 230).addBox(-7.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 16.0F));

        PartDefinition death_and_destruction2 = grinder.addOrReplaceChild("death_and_destruction2", CubeListBuilder.create().texOffs(210, 230).addBox(5.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(222, 230).addBox(2.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(12, 231).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 231).addBox(-4.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(190, 231).addBox(-7.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(232, 132).addBox(-10.0F, -2.0F, -2.0F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -4.0F, 11.0F));

        PartDefinition grinder_decorations = grinder.addOrReplaceChild("grinder_decorations", CubeListBuilder.create().texOffs(222, 12).addBox(-9.0F, -23.0F, 3.0F, 3.0F, 20.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(164, 142).addBox(-10.0F, -26.0F, 0.0F, 20.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(156, 57).addBox(-6.0F, -20.0F, 0.0F, 12.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(134, 208).addBox(6.0F, -23.0F, 1.0F, 3.0F, 23.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition grinder_frame = grinder.addOrReplaceChild("grinder_frame", CubeListBuilder.create().texOffs(182, 51).addBox(-10.0F, -7.0F, 18.0F, 18.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(204, 90).addBox(-10.0F, -7.0F, 8.0F, 18.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(200, 0).addBox(-7.0F, -10.005F, 3.0F, 14.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r8 = grinder_frame.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 192).addBox(-19.0F, -13.0F, 8.0F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(212, 56).addBox(-19.0F, -8.0F, 8.0F, 11.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(148, 63).addBox(-12.0F, -5.0F, -12.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(138, 46).addBox(-15.7F, -5.0F, -13.5F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(226, 203).addBox(-18.0F, -7.0F, -10.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r9 = grinder_frame.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(60, 202).addBox(-16.85F, -5.005F, -1.85F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(222, 35).addBox(-19.6F, -5.005F, -2.1625F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, 0.0F));

        PartDefinition what_is_this_thing = burner.addOrReplaceChild("what_is_this_thing", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition decoration_shit = what_is_this_thing.addOrReplaceChild("decoration_shit", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r10 = decoration_shit.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(234, 16).addBox(2.5F, -4.0F, 1.75F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(212, 188).addBox(-8.5F, -2.0F, 1.75F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.98F, -12.0F, -0.8006F, 0.0F, -0.5236F, 0.0F));

        PartDefinition cube_r11 = decoration_shit.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(224, 128).addBox(0.325F, -11.995F, 0.2125F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(234, 22).addBox(8.325F, -9.995F, 0.2125F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(138, 15).addBox(0.325F, -9.995F, 0.2125F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.3675F, -39.005F, -6.0184F, 0.0F, -1.0472F, 0.0F));

        PartDefinition cube_r12 = decoration_shit.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(62, 95).addBox(1.1375F, -1.004F, -1.6313F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.544F, -37.001F, -10.035F, 0.0F, -2.0071F, 0.0F));

        PartDefinition cube_r13 = decoration_shit.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(234, 12).addBox(12.0F, -38.001F, 1.6812F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(226, 215).addBox(10.0F, -38.001F, 1.6812F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(60, 209).addBox(5.0F, -17.001F, -1.8187F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 221).addBox(4.5F, -25.001F, -2.3187F, 4.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(150, 208).addBox(4.0F, -24.001F, -2.8187F, 5.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.0F, -23.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition wall_side4_r1 = decoration_shit.addOrReplaceChild("wall_side4_r1", CubeListBuilder.create().texOffs(224, 192).addBox(-9.5F, -4.005F, -3.0801F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.75F, -25.0F, 3.25F, 0.0F, -2.618F, 0.0F));

        PartDefinition cube_r14 = decoration_shit.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(178, 217).addBox(-1.4F, -9.995F, 12.65F, 11.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));

        PartDefinition cube_r15 = decoration_shit.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(186, 133).addBox(-3.4F, -10.0F, 5.05F, 18.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition cube_r16 = decoration_shit.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(210, 217).addBox(5.5F, -12.999F, 11.4313F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.75F, -0.25F, -23.0F, 0.7854F, -1.0472F, 0.0F));

        PartDefinition cube_r17 = decoration_shit.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(86, 32).addBox(6.0F, -36.001F, -13.3188F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, 0.25F, -23.0F, -0.5236F, -1.0472F, 0.0F));

        PartDefinition cube_r18 = decoration_shit.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 235).addBox(-0.37F, -2.9F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(28.1547F, -24.3377F, -3.3888F, 0.0F, -1.0472F, 0.0F));

        PartDefinition cube_r19 = decoration_shit.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(206, 231).addBox(-19.1021F, -24.7F, -0.9973F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.1821F, 14.001F, -16.1977F, -0.9828F, -0.4478F, 1.2898F));

        PartDefinition cube_r20 = decoration_shit.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(134, 202).addBox(-0.65F, -1.15F, -0.9973F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.1821F, -14.001F, -16.1977F, 0.9828F, -0.4478F, -1.2898F));

        PartDefinition cube_r21 = decoration_shit.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(234, 230).addBox(-0.25F, -3.15F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(26.8765F, -16.011F, -5.6027F, -0.6746F, -0.876F, 0.8055F));

        PartDefinition cube_r22 = decoration_shit.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(178, 208).addBox(-0.25F, -1.75F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0015F, -11.761F, -12.3144F, 1.0472F, 0.0F, -1.5708F));

        PartDefinition cube_r23 = decoration_shit.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(234, 26).addBox(-0.25F, -3.5F, -0.49F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(27.7218F, -19.9996F, -4.1386F, -0.3587F, -1.0075F, 0.4173F));

        PartDefinition cube_r24 = decoration_shit.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(160, 184).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.1821F, -18.001F, -18.1977F, 0.3587F, -1.0075F, -0.4173F));

        PartDefinition the_thing = what_is_this_thing.addOrReplaceChild("the_thing", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hexagon_Shape = the_thing.addOrReplaceChild("Hexagon_Shape", CubeListBuilder.create().texOffs(222, 45).addBox(-7.5F, -1.005F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.75F, -8.0F, 3.25F, 0.0F, -0.5236F, 0.0F));

        PartDefinition wall_side6_r1 = Hexagon_Shape.addOrReplaceChild("wall_side6_r1", CubeListBuilder.create().texOffs(170, 224).addBox(-7.5F, -0.995F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -5.236F, 0.0F));

        PartDefinition wall_side5_r1 = Hexagon_Shape.addOrReplaceChild("wall_side5_r1", CubeListBuilder.create().texOffs(150, 224).addBox(-7.5F, -1.005F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.1888F, 0.0F));

        PartDefinition wall_side4_r2 = Hexagon_Shape.addOrReplaceChild("wall_side4_r2", CubeListBuilder.create().texOffs(224, 117).addBox(-7.5F, -0.995F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition wall_side3_r1 = Hexagon_Shape.addOrReplaceChild("wall_side3_r1", CubeListBuilder.create().texOffs(224, 106).addBox(-7.5F, -1.005F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side2_r1 = Hexagon_Shape.addOrReplaceChild("wall_side2_r1", CubeListBuilder.create().texOffs(224, 95).addBox(-7.5F, -0.995F, -4.3301F, 1.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition Hexagon_Shape2 = the_thing.addOrReplaceChild("Hexagon_Shape2", CubeListBuilder.create().texOffs(138, 30).addBox(-8.25F, -14.005F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.75F, -10.0F, 3.25F, 0.0F, -1.0472F, 0.0F));

        PartDefinition wall_side7_r1 = Hexagon_Shape2.addOrReplaceChild("wall_side7_r1", CubeListBuilder.create().texOffs(202, 231).addBox(-8.25F, -13.995F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -5.236F, 0.0F));

        PartDefinition wall_side6_r2 = Hexagon_Shape2.addOrReplaceChild("wall_side6_r2", CubeListBuilder.create().texOffs(36, 231).addBox(-8.25F, -14.005F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -4.1888F, 0.0F));

        PartDefinition wall_side5_r2 = Hexagon_Shape2.addOrReplaceChild("wall_side5_r2", CubeListBuilder.create().texOffs(54, 230).addBox(-8.25F, -13.995F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition wall_side4_r3 = Hexagon_Shape2.addOrReplaceChild("wall_side4_r3", CubeListBuilder.create().texOffs(128, 227).addBox(-8.25F, -14.005F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side3_r2 = Hexagon_Shape2.addOrReplaceChild("wall_side3_r2", CubeListBuilder.create().texOffs(142, 30).addBox(-8.25F, -13.995F, -0.6699F, 1.0F, 15.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition Hexagon_Shape3 = the_thing.addOrReplaceChild("Hexagon_Shape3", CubeListBuilder.create().texOffs(164, 152).addBox(-7.5F, -3.995F, -4.3301F, 15.0F, 5.0F, 8.6603F, new CubeDeformation(0.0F))
                .texOffs(104, 216).addBox(-12.5F, -1.995F, -4.3301F, 5.0F, 2.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.75F, -25.0F, 3.25F, 0.0F, -0.5236F, 0.0F));

        PartDefinition wall_side4_r4 = Hexagon_Shape3.addOrReplaceChild("wall_side4_r4", CubeListBuilder.create().texOffs(164, 180).addBox(-7.5F, -4.005F, -4.3301F, 15.0F, 5.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side3_r3 = Hexagon_Shape3.addOrReplaceChild("wall_side3_r3", CubeListBuilder.create().texOffs(164, 166).addBox(-7.5F, -4.0F, -4.3301F, 15.0F, 5.0F, 8.6603F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition twelve_gon_Shape = the_thing.addOrReplaceChild("twelve_gon_Shape", CubeListBuilder.create().texOffs(186, 97).addBox(-7.5F, -6.015F, -2.0096F, 15.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offset(21.0F, -37.0F, 3.25F));

        PartDefinition wall_side6_r3 = twelve_gon_Shape.addOrReplaceChild("wall_side6_r3", CubeListBuilder.create().texOffs(182, 15).addBox(-8.0F, -5.99F, -2.0096F, 16.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.618F, 0.0F));

        PartDefinition wall_side5_r3 = twelve_gon_Shape.addOrReplaceChild("wall_side5_r3", CubeListBuilder.create().texOffs(186, 115).addBox(-7.5F, -5.995F, -2.0096F, 15.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side4_r5 = twelve_gon_Shape.addOrReplaceChild("wall_side4_r5", CubeListBuilder.create().texOffs(182, 33).addBox(-8.0F, -6.0F, -2.0096F, 16.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition wall_side3_r4 = twelve_gon_Shape.addOrReplaceChild("wall_side3_r4", CubeListBuilder.create().texOffs(102, 184).addBox(-7.5F, -6.005F, -2.0096F, 15.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition wall_side2_r2 = twelve_gon_Shape.addOrReplaceChild("wall_side2_r2", CubeListBuilder.create().texOffs(34, 184).addBox(-8.0F, -6.01F, -2.0096F, 16.0F, 14.0F, 4.0192F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition Hexagon_Shape4 = the_thing.addOrReplaceChild("Hexagon_Shape4", CubeListBuilder.create().texOffs(212, 66).addBox(-5.0F, 4.005F, -2.8867F, 10.0F, 4.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offset(21.0F, -51.0F, 3.0F));

        PartDefinition wall_side3_r5 = Hexagon_Shape4.addOrReplaceChild("wall_side3_r5", CubeListBuilder.create().texOffs(212, 162).addBox(-5.0F, 3.995F, -2.8867F, 10.0F, 4.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side2_r3 = Hexagon_Shape4.addOrReplaceChild("wall_side2_r3", CubeListBuilder.create().texOffs(212, 152).addBox(-5.0F, 4.0F, -2.8867F, 10.0F, 4.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition Hexagon_Shape5 = the_thing.addOrReplaceChild("Hexagon_Shape5", CubeListBuilder.create().texOffs(102, 202).addBox(-5.0F, -3.995F, -2.8867F, 10.0F, 8.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -20.0F, 3.25F, 0.0F, 0.5236F, 0.0F));

        PartDefinition wall_side3_r6 = Hexagon_Shape5.addOrReplaceChild("wall_side3_r6", CubeListBuilder.create().texOffs(204, 76).addBox(-5.0F, -4.005F, -2.8867F, 10.0F, 8.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));

        PartDefinition wall_side2_r4 = Hexagon_Shape5.addOrReplaceChild("wall_side2_r4", CubeListBuilder.create().texOffs(182, 203).addBox(-5.0F, -4.0F, -2.8867F, 10.0F, 8.0F, 5.7735F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition power = root.addOrReplaceChild("power", CubeListBuilder.create(), PartPose.offset(8.0F, 2.0F, -35.0F));

        PartDefinition indicator = power.addOrReplaceChild("indicator", CubeListBuilder.create().texOffs(214, 203).addBox(-9.5F, -46.0F, 4.0F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(92, 216).addBox(-9.5F, -46.0F, 4.0F, 3.0F, 24.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, 1.0F));

        PartDefinition support_r1 = indicator.addOrReplaceChild("support_r1", CubeListBuilder.create().texOffs(190, 224).addBox(-10.0F, -22.5F, 1.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition connection_point = power.addOrReplaceChild("connection_point", CubeListBuilder.create(), PartPose.offsetAndRotation(-50.2487F, 84.0F, -49.0F, 0.0F, 1.0472F, 0.0F));

        PartDefinition cube_r25 = connection_point.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(44, 202).addBox(-30.0F, -66.0F, 8.0F, 4.0F, 24.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -74.0F, 80.5F, 0.0F, -0.5236F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public ModelPart getPowerIndicatorPart() {
        return null;
    }
}
