package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import com.mojang.blaze3d.pipeline.RenderTarget;

public class DepthOnlyRenderTarget extends RenderTarget {
    public DepthOnlyRenderTarget(String label) {
        super(label, true, false);
    }
}