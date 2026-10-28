//package me.nova.client;
//
//import me.nova.NovaMod;
//import me.nova.entity.BlackCubeEntity;
//import me.nova.entity.BlackCubeEntity;
//import net.minecraft.client.render.RenderLayer;
//import net.minecraft.util.Identifier;
//import org.jetbrains.annotations.Nullable;
//import software.bernie.geckolib.model.GeoModel;
//import software.bernie.geckolib.renderer.base.GeoRenderState;
//
//public class BlackCubeModel extends GeoModel<BlackCubeEntity> {
//
//    @Override
//    public Identifier getModelResource(GeoRenderState renderState) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "black_cube"
//        );
//    }
//
//    @Override
//    public Identifier getTextureResource(GeoRenderState renderState) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "textures/entity/black_cube.png"
//        );
//    }
//
//    @Override
//    public Identifier getAnimationResource(BlackCubeEntity animatable) {
//        return Identifier.of(
//                NovaMod.MOD_ID,
//                "black_cube"
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