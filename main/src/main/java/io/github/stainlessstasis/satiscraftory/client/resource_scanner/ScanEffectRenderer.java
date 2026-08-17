package io.github.stainlessstasis.satiscraftory.client.resource_scanner;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalInt;

/**
 * Ported from <a href="https://github.com/MightyPirates/Scannable/blob/1.21.1/common/src/main/java/li/cil/scannable/client/renderer/ScannerRenderer.java">Scannable</a>
 */
public final class ScanEffectRenderer {
    public static final ScanEffectRenderer INSTANCE = new ScanEffectRenderer();
    private static final int UNIFORM_BUFFER_SIZE = 112; // mat4 (64) + 2x vec4 (16 each) + float, rounded up to a multiple of 16

    private GpuBuffer uniformBuffer;
    private GpuBuffer triangleBuffer;

    private long pingStartMillis = -1;
    private Vec3 pingCenter = Vec3.ZERO;

    private ScanEffectRenderer() {}

    public void ping(Vec3 center) {
        this.pingStartMillis = System.currentTimeMillis();
        this.pingCenter = center;
    }

    public void render(float scanRange, float scanDurationMillis) {
        if (!ScanPingRadius.isActive(pingStartMillis, scanDurationMillis)) return;

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        var colorView = mainTarget.getColorTextureView();
        if (colorView == null) return;

        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Matrix4f invViewProj = camera.getViewRotationProjectionMatrix(new Matrix4f()).invert();
        Vec3 cameraPos = camera.position();
        float radius = ScanPingRadius.computeRadius(pingStartMillis, scanDurationMillis, scanRange);

        ensureBuffers(device);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer uniformData = Std140Builder.onStack(stack, UNIFORM_BUFFER_SIZE)
                    .putMat4f(invViewProj)
                    .putVec4((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, 0f)
                    .putVec4((float) pingCenter.x, (float) pingCenter.y, (float) pingCenter.z, 0f)
                    .putFloat(radius)
                    .get();

            encoder.writeToBuffer(new GpuBufferSlice(uniformBuffer, 0, UNIFORM_BUFFER_SIZE), uniformData);

            try (RenderPass pass = encoder.createRenderPass(
                    () -> "scan_effect", colorView, OptionalInt.empty()
            )) {
                pass.setPipeline(ScanEffectPipeline.PIPELINE);
                pass.bindTexture("depthTex", mainTarget.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                pass.setUniform("ScanEffectUniforms", uniformBuffer);
                pass.setVertexBuffer(0, triangleBuffer);
                pass.draw(0, 3);
            }
        }
    }

    private void ensureBuffers(GpuDevice device) {
        if (triangleBuffer == null) {
            triangleBuffer = buildFullscreenTriangle(device);
        }
        if (uniformBuffer == null) {
            uniformBuffer = device.createBuffer(
                    () -> "scan_effect_uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    UNIFORM_BUFFER_SIZE
            );
        }
    }

    private GpuBuffer buildFullscreenTriangle(GpuDevice device) {
        ByteBuffer data = ByteBuffer.allocateDirect(3 * DefaultVertexFormat.POSITION_TEX.getVertexSize());
        data.order(ByteOrder.nativeOrder());

        putVertex(data, -1f, -1f, 0f, 0f);
        putVertex(data, 3f, -1f, 2f, 0f);
        putVertex(data, -1f, 3f, 0f, 2f);
        data.flip();

        return device.createBuffer(() -> "scan_effect_triangle", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, data);
    }

    private void putVertex(ByteBuffer buffer, float x, float y, float u, float v) {
        buffer.putFloat(x).putFloat(y).putFloat(0f);
        buffer.putFloat(u).putFloat(v);
    }

    public void close() {
        if (uniformBuffer != null) {
            uniformBuffer.close();
            uniformBuffer = null;
        }
        if (triangleBuffer != null) {
            triangleBuffer.close();
            triangleBuffer = null;
        }
    }
}