package me.nova.mixin;

import me.nova.client.BlackCubeEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 在世界、手持物和原版后处理完成后，GUI 实际提交前执行回忆过渡。 */
@Mixin(GameRenderer.class)
public abstract class BlackCubeGameRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"),
            require = 1, allow = 1
    )
    private void renderMemoryBeforeHud(RenderTickCounter counter, boolean tick, CallbackInfo info) {
        if (tick) {
            BlackCubeEffect.render(counter.getTickProgress(false));
        }
    }
}
