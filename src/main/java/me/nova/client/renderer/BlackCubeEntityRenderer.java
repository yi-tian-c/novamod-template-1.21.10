package me.nova.client.renderer;

import me.nova.entity.BlackCubeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BlackCubeEntityRenderer<
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<BlackCubeEntity, R> {

    public BlackCubeEntityRenderer(
            EntityRendererFactory.Context context,
            EntityType<BlackCubeEntity> entityType
    ) {
        super(context, entityType);

        this.shadowRadius = 0.3F;
    }
    @Override
    public RenderLayer getRenderType(
            R renderState,
            Identifier texture
    ) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}