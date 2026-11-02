package me.nova.entity;

import me.nova.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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

/** 可被收回的悬浮黑方块，服务端驱动回忆仪式，客户端插值显示缩放。 */
public class BlackCubeEntity extends LivingEntity implements GeoEntity {

    /** 仪式总时长，供客户端、粒子和服务端共用，单位为 tick。 */
    public static final int EFFECT_DURATION = 352;
    /** 烟雾环绕两点五秒后开始地形黑化。 */
    public static final int BLACKENING_START_TICK = 90;
    /** 地形渐黑四点六秒后完全黑屏。 */
    public static final int FULL_BLACK_TICK = 182;
    /** 黑场保持六点五秒后开始恢复。 */
    public static final int RECOVERY_START_TICK = 312;
    /** 上浮和模型放大共用的一秒时长。 */
    private static final int RISE_TICKS = 20;
    /** 同步的仪式 tick，负数表示空闲。 */
    private static final TrackedData<Integer> EFFECT_TICKS =
            DataTracker.registerData(BlackCubeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** 同步的本次点击玩家实体 ID，负数表示没有目标。 */
    private static final TrackedData<Integer> EFFECT_TARGET =
            DataTracker.registerData(BlackCubeEntity.class, TrackedDataHandlerRegistry.INTEGER);
    /** 原始悬浮高度，用于结束及存档恢复。 */
    private double restingY;
    /** 上一客户端 tick 的显示时钟。 */
    private float previousClientEffectTime = -1F;
    /** 当前客户端 tick 的显示时钟。 */
    private float clientEffectTime = -1F;
    /** 最近一次收到的同步进度，避免对旧数据反复校准。 */
    private int lastSyncedEffectTicks = -1;
    /** 当前实体独享的 GeckoLib 动画缓存。 */
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
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(EFFECT_TICKS, -1);
        builder.add(EFFECT_TARGET, -1);
    }

    /** 主手右键启动仪式，正在播放时不重置目标和进度。 */
    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND || player.isSpectator() || !isAlive()) {
            return ActionResult.PASS;
        }
        if (!getEntityWorld().isClient() && getEffectTicks() < 0) {
            restingY = getY();
            dataTracker.set(EFFECT_TARGET, player.getId());
            dataTracker.set(EFFECT_TICKS, 0);
        }
        return ActionResult.SUCCESS;
    }

    /** @return 当前仪式进度，负数表示空闲 */
    public int getEffectTicks() {
        return dataTracker.get(EFFECT_TICKS);
    }

    /** @return 本次点击的玩家，目标卸载或死亡后返回 null */
    public PlayerEntity getEffectTarget() {
        Entity entity = getEntityWorld().getEntityById(dataTracker.get(EFFECT_TARGET));
        return entity instanceof PlayerEntity player && player.isAlive() ? player : null;
    }

    /**
     * 获取平滑显示时钟，服务端调用时返回权威进度。
     *
     * @param tickDelta 当前帧位于客户端两次 tick 之间的比例
     * @return 显示进度，空闲时返回负数
     */
    public float getEffectTime(float tickDelta) {
        if (getEffectTicks() < 0) {
            return -1F;
        }
        return getEntityWorld().isClient() && clientEffectTime >= 0F
                ? MathHelper.lerp(MathHelper.clamp(tickDelta, 0F, 1F), previousClientEffectTime, clientEffectTime)
                : getEffectTicks();
    }

    /**
     * 获取模型显示缩放，不修改碰撞盒。
     *
     * @param tickDelta 帧内插值比例
     * @return 一至二点四倍的模型缩放
     */
    public float getEffectScale(float tickDelta) {
        float time = getEffectTime(tickDelta);
        if (time < 0F) {
            return 1F;
        }
        float rise = MathHelper.clamp(time / RISE_TICKS, 0F, 1F);
        float recovery = MathHelper.clamp((EFFECT_DURATION - time)
                / (EFFECT_DURATION - RECOVERY_START_TICK), 0F, 1F);
        return 1F + 1.4F * rise * rise * (3F - 2F * rise) * recovery;
    }

    /** 显示时钟单调推进，新同步包只改变追赶速度，不直接跳改当前帧。 */
    private void tickClientEffectAnimation() {
        int syncedTicks = getEffectTicks();
        if (syncedTicks < 0) {
            previousClientEffectTime = -1F;
            clientEffectTime = -1F;
            lastSyncedEffectTicks = -1;
        } else if (clientEffectTime < 0F) {
            previousClientEffectTime = syncedTicks;
            clientEffectTime = syncedTicks;
            lastSyncedEffectTicks = syncedTicks;
        } else {
            previousClientEffectTime = clientEffectTime;
            float advance = 1F;
            if (syncedTicks != lastSyncedEffectTicks) {
                advance += MathHelper.clamp((syncedTicks - clientEffectTime - 1F) * 0.15F, -0.25F, 0.25F);
                lastSyncedEffectTicks = syncedTicks;
            }
            clientEffectTime = Math.min(clientEffectTime + advance, EFFECT_DURATION);
        }
    }

    /** 保存原始高度，避免在播放期间卸载区块后永久抬高本体。 */
    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (getEffectTicks() >= 0) {
            view.putDouble("BlackCubeRestingY", restingY);
        }
    }

    /** 重载时恢复原始高度，取消不应持久化的临时仪式。 */
    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        setPosition(getX(), view.getDouble("BlackCubeRestingY", getY()), getZ());
    }
    @Override
    public boolean shouldRenderName() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // 持续关闭重力，确保实体能够悬浮
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
        if (getEntityWorld().isClient()) {
            tickClientEffectAnimation();
        } else if (getEffectTicks() >= 0) {
            int ticks = getEffectTicks() + 1;
            if (ticks >= EFFECT_DURATION || getEffectTarget() == null) {
                setPosition(getX(), restingY, getZ());
                dataTracker.set(EFFECT_TICKS, -1);
                dataTracker.set(EFFECT_TARGET, -1);
            } else {
                dataTracker.set(EFFECT_TICKS, ticks);
                double rise = MathHelper.clamp(ticks / (double) RISE_TICKS, 0.0, 1.0);
                double recovery = MathHelper.clamp((EFFECT_DURATION - ticks)
                        / (double) (EFFECT_DURATION - RECOVERY_START_TICK), 0.0, 1.0);
                setPosition(getX(), restingY + 1.8 * rise * rise * (3.0 - 2.0 * rise) * recovery, getZ());
            }
        }
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
