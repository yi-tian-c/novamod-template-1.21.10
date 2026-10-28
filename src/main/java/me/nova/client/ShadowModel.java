//package me.nova.client;
//
//import me.nova.NovaMod;
//import me.nova.entity.ShadowEntity;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.util.Identifier;
//import org.jetbrains.annotations.Nullable;
//import software.bernie.geckolib.model.GeoModel;
//import software.bernie.geckolib.renderer.base.GeoRenderState;
//
//public class ShadowModel extends GeoModel<ShadowEntity> {
//
//    @Override
//    public Identifier getModelResource(GeoRenderState renderState) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "shadow"
//        );
//    }
//
//    @Override
//    public Identifier getTextureResource(GeoRenderState renderState) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "textures/entity/shadow.png"
//        );
//    }
//
//    @Override
//    public Identifier getAnimationResource(ShadowEntity animatable) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "shadow"
//        );
//    }
//
//    @Override
//    public @Nullable RenderLayer getRenderType(
//            GeoRenderState renderState,
//            Identifier texture
//    ) {
//        return RenderLayer.getEntityCutoutNoCull(texture);
//    }
//}