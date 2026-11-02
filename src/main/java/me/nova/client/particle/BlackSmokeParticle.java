package me.nova.client.particle;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import me.nova.entity.BlackCubeEntity;

/** 动画黑烟，可从黑方块向外喷出，再追踪并环绕指定玩家。 */
public final class BlackSmokeParticle extends BillboardParticle {
    /** 黑烟的逐帧动画精灵。 */
    private final SpriteProvider sprites;
    /** 每个烟团内固定的空间偏移，避免所有粒子汇聚到同一点。 */
    private final Vec3d cloudOffset;
    /** 每个粒子独立的基础大小。 */
    private final float baseSize;
    /** 激活本粒子的黑方块；普通指令生成的黑烟没有来源。 */
    private BlackCubeEntity source;
    /** 烟雾需要追踪的目标玩家。 */
    private PlayerEntity target;
    /** 喷射时的世界坐标，固定后不再随本体上浮而移动。 */
    private Vec3d launchPosition;
    /** 同一簇烟雾共享的喷射、环绕相位，单位为弧度。 */
    private double phase;
    /** 直接补充玩家烟层的粒子跳过喷射阶段。 */
    private boolean enclosing;

    /** 创建带独立旋转、大小和动画起始状态的黑烟。 */
    public BlackSmokeParticle(ClientWorld world, double x, double y, double z,
                              double velocityX, double velocityY, double velocityZ, SpriteProvider sprites) {
        super(world, x, y, z, sprites.getFirst());
        this.sprites = sprites;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        collidesWithWorld = false;
        maxAge = 64 + random.nextInt(20);
        baseSize = 0.2F + random.nextFloat() * 0.18F;
        scale = baseSize;
        alpha = 0F;
        cloudOffset = new Vec3d(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).multiply(0.4);
        zRotation = random.nextFloat() * MathHelper.TAU;
        lastZRotation = zRotation;
        float shade = 0.015F + random.nextFloat() * 0.025F;
        setColor(shade, shade, shade);
        updateSprite(sprites);
    }

    /**
     * 将新生粒子绑定到一次仪式；应在加入粒子管理器的同一客户端 tick 调用。
     *
     * @param source 仪式来源，必须与粒子处于同一世界
     * @param target 本次右键玩家，逐 tick 追踪其当前位置
     * @param phase 同簇粒子共享的相位，单位为弧度
     * @param enclosing 为 true 时直接加入玩家周围的烟层
     */
    public void bind(BlackCubeEntity source, PlayerEntity target, double phase, boolean enclosing) {
        this.source = source;
        this.target = target;
        this.phase = phase;
        this.enclosing = enclosing;
        launchPosition = new Vec3d(x, y, z);
        if (enclosing) {
            followRitual();
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    /** 推进追踪轨迹及烟雾形变，并在来源、目标或寿命失效时回收粒子。 */
    @Override
    public void tick() {
        lastX = x;
        lastY = y;
        lastZ = z;
        lastZRotation = zRotation;
        if (++age >= maxAge || (source != null && (!source.isAlive() || source.getEntityWorld() != world
                || source.getEffectTicks() < 0 || target == null || !target.isAlive()
                || target.getEntityWorld() != world))) {
            markDead();
            return;
        }
        if (source == null) {
            setPos(x + velocityX, y + velocityY + 0.012, z + velocityZ);
            velocityX *= 0.96;
            velocityZ *= 0.96;
        } else {
            followRitual();
        }
        // 大小脉动、旋转与逐帧纹理一起改变烟团轮廓，前后淡化避免整片突然出现。
        float life = age / (float) maxAge;
        scale = baseSize * (1F + life * 1.8F) * (1F + 0.12F * (float) Math.sin(age * 0.21 + phase));
        alpha = Math.min(age / 5F, 1F) * Math.min((maxAge - age) / 18F, 1F) * 0.58F;
        if (source != null) {
            alpha *= MathHelper.clamp((BlackCubeEntity.EFFECT_DURATION - source.getEffectTicks())
                    / (float) (BlackCubeEntity.EFFECT_DURATION - BlackCubeEntity.RECOVERY_START_TICK), 0F, 1F);
        }
        zRotation += 0.014F * (float) Math.sin(phase + age * 0.08);
        updateSprite(sprites);
    }

    /** 在世界坐标中计算喷射、转向和收紧的连续轨迹，目标位置每 tick 重新读取。 */
    private void followRitual() {
        float enclosure = MathHelper.clamp((source.getEffectTicks() - BlackCubeEntity.BLACKENING_START_TICK)
                / (float) (BlackCubeEntity.FULL_BLACK_TICK - BlackCubeEntity.BLACKENING_START_TICK), 0F, 1F);
        double orbitAngle = phase + age * (0.1 + enclosure * 0.1);
        double radius = MathHelper.lerp(enclosure, 2.4, 0.55);
        double height = 1.0 + Math.sin(phase * 1.7 + age * 0.09) * 1.1;
        Vec3d orbit = target.getEntityPos().add(Math.cos(orbitAngle) * radius, height, Math.sin(orbitAngle) * radius);
        Vec3d destination;
        if (!enclosing && age < 7) {
            // 先沿簇的径向喷出，同簇粒子保持邻近，形成气团而非散点。
            double burst = age / 7.0;
            destination = launchPosition.add(Math.cos(phase) * 2.7 * burst, burst * 0.65,
                    Math.sin(phase) * 2.7 * burst);
        } else if (!enclosing && age < 23) {
            double travel = (age - 7) / 16.0;
            travel = travel * travel * (3.0 - 2.0 * travel);
            Vec3d burstEnd = launchPosition.add(Math.cos(phase) * 2.7, 0.65, Math.sin(phase) * 2.7);
            destination = burstEnd.lerp(orbit, travel);
        } else {
            destination = orbit;
        }
        double turbulence = Math.sin(age * 0.16 + phase) * 0.12;
        destination = destination.add(cloudOffset.multiply(1.0 - enclosure * 0.45))
                .add(turbulence, Math.cos(age * 0.12 + phase) * 0.1, -turbulence);
        setPos(destination.x, destination.y, destination.z);
    }

    @Override
    protected RenderType getRenderType() {
        return BlackSmokeRenderType.TYPE;
    }
}
