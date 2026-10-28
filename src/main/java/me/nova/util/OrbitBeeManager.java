package me.nova.util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class OrbitBeeManager {

    private OrbitBeeManager() {}

    private static final Map<UUID, OrbitData> ORBITS = new HashMap<>();
    // 上下浮动参数
    private static final double BOB_PHASE_STEP = 120;
    private static final double BOB_AMPLITUDE = 0.25; // 上下振幅，单位：格
    private static final double BOB_SPEED = 0.2;     // 浮动速度
    // 可调整的参数
    private static final double RADIUS = 1.5;
    private static final double HEIGHT = 1.2;
    private static final double ANGULAR_SPEED = 0.08;

    // 蜜蜂数量
    private static final int BEE_COUNT = 3;

    // 正三角形相邻顶点之间的角度：360 / 3 = 120 度
    private static final double ANGLE_STEP = Math.PI * 2.0 / BEE_COUNT;

    private static class OrbitData {
        final ServerWorld world;
        final BeeEntity bee;
        final Entity target;
        final double bobPhase;
        double angle;

        OrbitData(ServerWorld world, BeeEntity bee, Entity target,
                  double angle, double bobPhase) {
            this.world = world;
            this.bee = bee;
            this.target = target;
            this.angle = angle;
            this.bobPhase = bobPhase;
        }
    }

    /**
     * 一次生成三只蜜蜂，围绕目标实体构成正三角形。
     */
    public static void create(ServerWorld world, Entity target) {

        // 随机决定整个三角形的初始旋转角度
        double startAngle = Math.random() * Math.PI * 2.0;

        for (int i = 0; i < BEE_COUNT; i++) {

            BeeEntity bee = EntityType.BEE.create(
                    world,
                    SpawnReason.TRIGGERED
            );

            if (bee == null) {
                continue;
            }

            // 三只蜜蜂分别相差 120 度
            double angle = startAngle + i * ANGLE_STEP;

            double x = target.getX() + Math.cos(angle) * RADIUS;
            double y = target.getY() + HEIGHT;
            double z = target.getZ() + Math.sin(angle) * RADIUS;

            bee.refreshPositionAndAngles(
                    x, y, z,
                    0.0F, 0.0F
            );

            bee.setNoGravity(true);
            bee.setVelocity(Vec3d.ZERO);
            bee.setAiDisabled(true);
            bee.setSilent(true);

            if (world.spawnEntity(bee)) {
                ORBITS.put(
                        bee.getUuid(),
                        new OrbitData(
                                world,
                                bee,
                                target,
                                angle,
                                i * BOB_PHASE_STEP
                        )
                );
            }
        }
    }

    /**
     * 每 tick 更新蜜蜂位置。
     */
    public static void tick(ServerWorld world) {

        Iterator<Map.Entry<UUID, OrbitData>> iterator =
                ORBITS.entrySet().iterator();

        while (iterator.hasNext()) {
            OrbitData data = iterator.next().getValue();

            if (data.world != world) {
                continue;
            }

            BeeEntity bee = data.bee;
            Entity target = data.target;

            if (!bee.isAlive()
                    || !target.isAlive()
                    || target.getEntityWorld() != world) {
                iterator.remove();
                continue;
            }

            data.angle += ANGULAR_SPEED;

            double angle = data.angle;

            double x = target.getX() + Math.cos(angle) * RADIUS;
            double y = target.getY() + HEIGHT
                    + Math.sin(world.getTime() * BOB_SPEED + data.bobPhase)
                    * BOB_AMPLITUDE;
            double z = target.getZ() + Math.sin(angle) * RADIUS;

            // 圆周切线方向
            double tangentX = -Math.sin(angle);
            double tangentZ = Math.cos(angle);
            float yaw = (float) (
                    MathHelper.atan2(tangentZ, tangentX)
                            * (180.0 / Math.PI) - 90.0
            );            // 直接设置位置，避免 AI 和重力影响轨迹
            bee.setNoGravity(true);
            bee.setPosition(x, y, z);

            // 身体、头部朝向飞行方向
            bee.setYaw(yaw);
            bee.setBodyYaw(yaw);
            bee.setHeadYaw(yaw);
            bee.setPitch(0.0F);
        }
    }
}