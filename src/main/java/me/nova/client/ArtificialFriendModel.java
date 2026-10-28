package me.nova.client;

import me.nova.NovaMod;
import me.nova.entity.ArtificialFriendEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ArtificialFriendModel extends GeoModel<ArtificialFriendEntity> {

    private static final DataTicket<Integer> TEXTURE_INDEX =
            DataTicket.create("novamod:texture_index", Integer.class);

    @Override
    public void addAdditionalStateData(
            ArtificialFriendEntity entity,
            GeoRenderState renderState
    ) {
        renderState.addGeckolibData(
                TEXTURE_INDEX,
                entity.getTextureIndex()
        );
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        int index = renderState.getOrDefaultGeckolibData(TEXTURE_INDEX, 1);
        return NovaMod.id("textures/entity/af" + (index) + ".png");
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return NovaMod.id("entity/af");
    }

    @Override
    public Identifier getAnimationResource(ArtificialFriendEntity entity) {
        return NovaMod.id("entity/af");
    }
}