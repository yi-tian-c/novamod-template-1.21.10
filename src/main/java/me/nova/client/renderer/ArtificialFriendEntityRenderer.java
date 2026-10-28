package me.nova.client.renderer;

import me.nova.client.ArtificialFriendModel;
import me.nova.entity.ArtificialFriendEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ArtificialFriendEntityRenderer<
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<ArtificialFriendEntity, R> {

    public ArtificialFriendEntityRenderer(
            EntityRendererFactory.Context context,
            net.minecraft.entity.EntityType<ArtificialFriendEntity> entityType
    ) {
        super(context, new ArtificialFriendModel());

        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderLayer getRenderType(
            R renderState,
            Identifier texture
    ) {
        return RenderLayer.getEntityTranslucent(texture);
    }
}