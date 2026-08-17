package io.github.stainlessstasis.manifold.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ModelBoundsUtil {
    private static final Map<ModelPart, Vec3> CACHE = new IdentityHashMap<>();

    private ModelBoundsUtil() {}

    public static Vec3 getOrComputeHorizontalCenter(ModelPart root) {
        return CACHE.computeIfAbsent(root, ModelBoundsUtil::computeHorizontalCenter);
    }

    private static Vec3 computeHorizontalCenter(ModelPart root) {
        // minX, minZ, maxX, maxZ
        float[] bounds = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};

        root.getExtentsForGui(new PoseStack(), pos -> {
            bounds[0] = Math.min(bounds[0], pos.x());
            bounds[1] = Math.min(bounds[1], pos.z());
            bounds[2] = Math.max(bounds[2], pos.x());
            bounds[3] = Math.max(bounds[3], pos.z());
        });

        if (bounds[0] > bounds[2]) {
            return Vec3.ZERO; // no visible geometry
        }

        float centerX = (bounds[0] + bounds[2]) / 2f;
        float centerZ = (bounds[1] + bounds[3]) / 2f;
        return new Vec3(centerX, 0, centerZ);
    }
}