package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.stainlessstasis.satiscraftory.Satiscraftory;

public final class ScanEffectPipeline {
    private ScanEffectPipeline() {}

    public static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Satiscraftory.id("scan_effect"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Satiscraftory.id("core/scan_effect"))
            .withSampler("depthTex")
            .withUniform("ScanEffectUniforms", UniformType.UNIFORM_BUFFER)
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withCull(false)
            .build();

}