package dev.purifiedundead.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class FerinSlashParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private FerinSlashParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed * 0.08D;
        this.yd = ySpeed * 0.08D;
        this.zd = zSpeed * 0.08D;
        this.lifetime = 6;
        this.quadSize = 0.96F + random.nextFloat() * 0.28F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.rCol = 0.82F;
        this.gCol = 0.58F;
        this.bCol = 1.0F;
        this.alpha = 0.92F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        this.alpha = 0.92F * (1.0F - (float) age / lifetime);
        this.quadSize *= 1.045F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            return new FerinSlashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
