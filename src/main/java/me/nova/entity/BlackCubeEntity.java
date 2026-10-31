package me.nova.entity;

import me.nova.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BlackCubeEntity extends LivingEntity implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public BlackCubeEntity(
            EntityType<? extends LivingEntity> entityType,
            World world
    ) {
        super(entityType, world);
        this.setNoGravity(true);
    }
    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0);
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
     * @param state 包含BlackCubeEntity的AnimationTest状态对象
     * @return 返回PlayState.CONTINUE以继续动画播放
     */

    private PlayState predicate(  // 包含BlackCubeEntity的动画测试状态
                                  AnimationTest<BlackCubeEntity> state
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
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // 只响应玩家的攻击
        if (source.getAttacker() instanceof PlayerEntity player) {
            // 服务端生成掉落物，避免客户端重复生成
            if (!world.isClient()) {
                ItemStack stack = new ItemStack(ModItems.BLACK_CUBE);

                ItemEntity itemEntity = new ItemEntity(
                        world,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        stack
                );

                world.spawnEntity(itemEntity);

                // 移除黑色立方体实体
                this.discard();
            }

            return true;
        }

        return super.damage(world, source, amount);
    }
}