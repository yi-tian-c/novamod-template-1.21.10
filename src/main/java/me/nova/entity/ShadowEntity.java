package me.nova.entity;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationState;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ShadowEntity extends LivingEntity implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public ShadowEntity(
            EntityType<? extends LivingEntity> entityType,
            World world
    ) {
        super(entityType, world);
    }
    @Override
    public void tick() {
        super.tick();

        // 持续关闭重力，确保实体能够悬浮
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);

    }

    @Override
    public Arm getMainArm() {
        return null;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0);
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "controller",
                        0,
                        this::predicate
                )
        );
    }
/**
 * 根据给定的动画状态设置动画控制器
 * @param state 包含ShadowEntity的AnimationTest状态对象
 * @return 返回PlayState.CONTINUE以继续动画播放
 */

    private PlayState predicate(  // 包含ShadowEntity的动画测试状态
            AnimationTest<ShadowEntity> state
    // 设置动画控制器的动画，开始循环播放"idle"动画
    ) {
        state.controller().setAnimation(
                RawAnimation.begin()
                        .thenLoop("idle")
        );
    // 设置动画播放速度为1.0F（正常速度）

        state.controller().setAnimationSpeed(1.0F);
    // 继续播放动画状态

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    @Override
    public void takeKnockback(double strength, double x, double z) {
        // 忽略所有击退
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        // 不被其他实体推挤
    }
}