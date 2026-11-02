package me.nova.client.particle;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import me.nova.NovaMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.texture.SpriteAtlasTexture;

/** 复用新版粒子程序和图集，黑烟单独使用不读写深度的透明管线。 */
public final class BlackSmokeRenderType {
    /** 原生渲染管线的注册单例，随原版着色器资源一起重载。 */
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                    .withLocation(NovaMod.id("pipeline/black_smoke"))
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .withCull(false)
                    .build());
    /** 粒子提交器按同一身份分批，其他原版粒子的深度行为不变。 */
    public static final BillboardParticle.RenderType TYPE =
            new BillboardParticle.RenderType(true, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE, PIPELINE);

    private BlackSmokeRenderType() {
    }
}
