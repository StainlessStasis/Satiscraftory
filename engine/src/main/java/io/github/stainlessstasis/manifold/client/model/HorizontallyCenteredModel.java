package io.github.stainlessstasis.manifold.client.model;

import io.github.stainlessstasis.manifold.client.util.ModelBoundsUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

public interface HorizontallyCenteredModel {
    ModelPart root();

    default Vec3 getHorizontalCenter() {
        return ModelBoundsUtil.getOrComputeHorizontalCenter(this.root());
    }
}