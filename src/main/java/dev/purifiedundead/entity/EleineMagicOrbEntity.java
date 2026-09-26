package dev.purifiedundead.entity;

import dev.purifiedundead.combat.ModDamageTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

/** Visible, finite-life magic orb with mild steering and normal wall collision. */
public final class EleineMagicOrbEntity extends AbstractHurtingProjectile {
    private static final ParticleOptions EMBER = new net.minecraft.core.particles.DustParticleOptions(
            new org.joml.Vector3f(0.65F, 0.025F, 0.04F), 0.65F);
    private static final ParticleOptions SOOT = new net.minecraft.core.particles.DustParticleOptions(
            new org.joml.Vector3f(0.035F, 0.012F, 0.02F), 1.15F);
    private final java.util.List<Vec3> trail = new java.util.ArrayList<>();
    private final java.util.List<Vec3> trailView = java.util.Collections.unmodifiableList(trail);

    public java.util.List<Vec3> trailPositions() { return trailView; }
    public static final double SPEED = 0.55D;
    public static final double TURN_STRENGTH = 0.12D;
    public static final int LIFE_TICKS = 60;

    private UUID targetId;
    private float damage;

    public EleineMagicOrbEntity(EntityType<? extends EleineMagicOrbEntity> type, Level level) {
        super(type, level);
    }

    public void configure(Player owner, LivingEntity target, float damage) {
        setOwner(owner);
        targetId = target.getUUID();
        this.damage = damage;
        Vec3 start = owner.getEyePosition().add(owner.getLookAngle().scale(0.45D));
        setPos(start.x, start.y - 0.15D, start.z);
        Vec3 direction = target.getEyePosition().subtract(position()).normalize();
        setDeltaMovement(direction.scale(SPEED));
        xPower = 0.0D;
        yPower = 0.0D;
        zPower = 0.0D;
    }

    @Override
    public void tick() {
        if (level().isClientSide()) {
            trail.add(0, position());
            if (trail.size() > 10) trail.remove(trail.size() - 1);
            level().addParticle(EMBER,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
        if (!level().isClientSide()) {
            if (tickCount >= LIFE_TICKS) {
                discard();
                return;
            }
            if (targetId != null && level() instanceof ServerLevel serverLevel
                    && serverLevel.getEntity(targetId) instanceof LivingEntity target && target.isAlive()) {
                Vec3 desired = target.getEyePosition().subtract(position());
                Vec3 current = getDeltaMovement();
                if (desired.lengthSqr() > 1.0E-6D && current.lengthSqr() > 1.0E-6D) {
                    Vec3 steered = current.normalize().lerp(desired.normalize(), TURN_STRENGTH);
                    if (steered.lengthSqr() > 1.0E-6D) {
                        setDeltaMovement(steered.normalize().scale(SPEED));
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || !(getOwner() instanceof Player owner)
                || !(entity instanceof LivingEntity target) || target == owner
                || !target.isAlive() || !target.isAttackable() || owner.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player otherPlayer && !owner.canHarmPlayer(otherPlayer)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!level().isClientSide() && getOwner() instanceof Player owner
                && hit.getEntity() instanceof LivingEntity target && damage > 0.0F) {
            target.hurt(ModDamageTypes.eleineMagicOrb(level(), this, owner), damage);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide()) {
            discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return SOOT;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetId != null) {
            tag.putUUID("target", targetId);
        }
        tag.putFloat("damage", damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetId = tag.hasUUID("target") ? tag.getUUID("target") : null;
        damage = Math.max(0.0F, tag.getFloat("damage"));
    }
}
