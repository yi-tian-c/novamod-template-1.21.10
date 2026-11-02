package me.nova.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.nova.client.BlackCubeEffect;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 在手持物清除深度前捕获与世界绘制完全对应的矩阵和深度。 */
@Mixin(WorldRenderer.class)
public abstract class BlackCubeWorldRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/util/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
            at = @At("TAIL"), require = 1, allow = 1
    )
    private void captureMemoryTerrain(ObjectAllocator allocator, RenderTickCounter counter, boolean outlines,
                                      Camera camera, Matrix4f view, Matrix4f projection, Matrix4f culling,
                                      GpuBufferSlice fog, Vector4f fogColor, boolean renderSky, CallbackInfo info) {
        BlackCubeEffect.captureWorld(view, projection);
    }
}
