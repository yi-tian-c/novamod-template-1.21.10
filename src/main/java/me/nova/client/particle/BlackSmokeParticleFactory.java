package me.nova.client.particle;

import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.random.Random;

/** 在资源重载更新后的原版图集中创建八帧像素黑烟。 */
public final class BlackSmokeParticleFactory implements ParticleFactory<SimpleParticleType> {
    /** 原版资源重载管理的精灵提供者。 */
    private final SpriteProvider sprites;

    /** 保存本类型对应的精灵。 */
    public BlackSmokeParticleFactory(SpriteProvider sprites) {
        this.sprites = sprites;
    }

    @Override
    public BlackSmokeParticle createParticle(SimpleParticleType type, ClientWorld world,
                                            double x, double y, double z,
                                            double velocityX, double velocityY, double velocityZ, Random random) {
        return new BlackSmokeParticle(world, x, y, z, velocityX, velocityY, velocityZ, sprites);
    }
}
