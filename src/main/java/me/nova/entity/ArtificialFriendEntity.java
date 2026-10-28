package me.nova.entity;

import me.nova.NovaMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ArtificialFriendEntity extends Entity implements GeoEntity {

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private static final TrackedData<Integer> POSE_INDEX =
            DataTracker.registerData(
                    ArtificialFriendEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );
    private static final TrackedData<Integer> TEXTURE_INDEX =
            DataTracker.registerData(
                    ArtificialFriendEntity.class,
                    TrackedDataHandlerRegistry.INTEGER
            );
    public ArtificialFriendEntity(
            EntityType<? extends ArtificialFriendEntity> entityType,
            World world
    ) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(POSE_INDEX, 0);
        builder.add(TEXTURE_INDEX, 0);
    }

    @Override
    public void tick() {
        super.tick();

        // 始终保持悬浮和静止
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
    }

    @Override
    public boolean damage(
            ServerWorld world,
            DamageSource source,
            float amount
    ) {
        // 不接受任何伤害
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {

        int pose = view.getInt("PoseIndex", 1);
        int texture = view.getInt("TextureIndex", 1);

        System.out.println("读取 AF：pose=" + pose
                + ", texture=" + texture);

        setPoseIndex(pose);
        setTextureIndex(texture);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        System.out.println("保存 AF：pose=" + getPoseIndex()
                + ", texture=" + getTextureIndex());

        view.putInt("PoseIndex", getPoseIndex());
        view.putInt("TextureIndex", getTextureIndex());
    }

    public int getTextureIndex() {
        return this.dataTracker.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int index) {
        this.dataTracker.set(TEXTURE_INDEX, Math.floorMod(index - 1, 7) + 1);
    }

    public int getPoseIndex() {
        return this.dataTracker.get(POSE_INDEX);
    }

    public void setPoseIndex(int index) {
        int value = Math.floorMod(index - 1, 11) + 1;

        this.dataTracker.set(POSE_INDEX, value);

        System.out.println(
                "[AF] 设置姿势：输入=" + index
                        + ", 实际=" + getPoseIndex()
                        + ", 客户端=" + this.getEntityWorld().isClient()
        );
    }


    public void nextPose() {
        setPoseIndex(getPoseIndex() >= 11 ? 1 : getPoseIndex() + 1);
    }

    public void previousPose() {
        setPoseIndex(getPoseIndex() <= 1 ? 11 : getPoseIndex() - 1);
    }

    public void nextTexture() {
        setTextureIndex(getTextureIndex() >= 7 ? 1 : getTextureIndex() + 1);
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

    private PlayState predicate(
            AnimationTest<ArtificialFriendEntity> state
    ) {
        state.controller().setAnimation(
                RawAnimation.begin().then(
                        "pose" + getPoseIndex(),
                        Animation.LoopType.HOLD_ON_LAST_FRAME
                )
        );

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}