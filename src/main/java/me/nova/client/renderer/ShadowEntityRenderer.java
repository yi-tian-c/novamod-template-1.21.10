package me.nova.client.renderer;

import me.nova.entity.ShadowEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ShadowEntityRenderer<
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<ShadowEntity, R> {

    public ShadowEntityRenderer(
            EntityRendererFactory.Context context,
            EntityType<ShadowEntity> entityType
    ) {
        super(context, entityType);

        this.shadowRadius = 0.3F;
    }
    @Override
    public int getRenderColor(
            ShadowEntity animatable,
            Void relatedObject,
            float partialTick
    ) {
        float alpha = 0.6F + 0.4F * (float) Math.sin(
                (animatable.age + partialTick) * 0.1F
        );

        int alpha255 = (int) (alpha * 255.0F);

        return (alpha255 << 24) | 0xFFFFFF;
    }
    @Override
    public RenderLayer getRenderType(
            R renderState,
            Identifier texture
    ) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}