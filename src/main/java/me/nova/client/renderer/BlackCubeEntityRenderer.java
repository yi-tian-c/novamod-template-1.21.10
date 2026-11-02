package me.nova.client.renderer;

import me.nova.entity.BlackCubeEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.state.CameraRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/** 通过 GeckoLib 渲染状态快照平滑放大黑方块模型。 */
public class BlackCubeEntityRenderer<
        R extends EntityRenderState & GeoRenderState
        > extends GeoEntityRenderer<BlackCubeEntity, R> {

    /** 采集和绘制阶段共享的缩放数据键，避免渲染时回读活实体。 */
    private static final DataTicket<Float> EFFECT_SCALE = DataTicket.create("novamod_memory_scale", Float.class);
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
    @Override
    public boolean hasLabel(BlackCubeEntity entity, double squaredDistanceToCamera) {
        return false;
    }

    /** 在新版渲染状态采集阶段保存插值后的本体尺寸。 */
    @Override
    public void addRenderData(BlackCubeEntity entity, Void relatedObject, R state, float tickDelta) {
        super.addRenderData(entity, relatedObject, state, tickDelta);
        state.addGeckolibData(EFFECT_SCALE, entity.getEffectScale(tickDelta));
    }

    /** 使用该帧快照缩放模型，不将尺寸存到共享渲染器实例上。 */
    @Override
    public void scaleModelForRender(R state, float width, float height, MatrixStack matrices,
                                    BakedGeoModel model, CameraRenderState cameraState) {
        float scale = state.getGeckolibData(EFFECT_SCALE);
        super.scaleModelForRender(state, width * scale, height * scale, matrices, model, cameraState);
    }
}
