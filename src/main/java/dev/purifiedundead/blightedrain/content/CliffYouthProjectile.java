package dev.purifiedundead.blightedrain.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public final class CliffYouthProjectile extends ThrowableProjectile {
    private float damage = 7;
    private int punch;
    private boolean flame;

    public CliffYouthProjectile(EntityType<? extends CliffYouthProjectile> type, Level level) {
        super(type, level);
    }

    public void configure(Player owner, ItemStack weapon) {
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - .1, owner.getZ());
        int power = CliffYouthItem.enchantment(weapon, level(), Enchantments.POWER_ARROWS);
        damage = 7 + (power > 0 ? .5F * power + .5F : 0);
        punch = CliffYouthItem.enchantment(weapon, level(), Enchantments.PUNCH_ARROWS);
        flame = CliffYouthItem.enchantment(weapon, level(), Enchantments.FLAMING_ARROWS) > 0;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 200) discard();
    }

    private boolean eligible(LivingEntity target) {
        if (!target.isAlive() || target == getOwner() || !target.isAttackable()) return false;
        if (getOwner() instanceof Player player) {
            if (player.isAlliedTo(target)) return false;
            if (target instanceof Player other && !player.canHarmPlayer(other)) return false;
            if (target instanceof OwnableEntity owned
                    && player.getUUID().equals(owned.getOwnerUUID())) return false;
        }
        return true;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level().isClientSide || isRemoved()) return;
        setPos(hit.getLocation());
        var source = damageSources().thrown(this, getOwner());
        for (var target :
                level().getEntitiesOfClass(
                                LivingEntity.class, getBoundingBox().inflate(2), this::eligible)) {
            if (target.getBoundingBox().getCenter().distanceToSqr(position()) > 4) continue;
            if (!target.hurt(source, damage)) continue;
            target.addEffect(
                    new net.minecraft.world.effect.MobEffectInstance(
                            dev.purifiedundead.content.ModEffects.STUNNED.get(), 60, 0));
            if (flame) target.setSecondsOnFire(5);
            if (punch > 0)
                target.knockback(.6 * punch, getX() - target.getX(), getZ() - target.getZ());
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server)
            server.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    getX(),
                    getY(),
                    getZ(),
                    20,
                    .7,
                    .2,
                    .7,
                    .05);
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ImpactDamage", damage);
        tag.putInt("Punch", punch);
        tag.putBoolean("Flame", flame);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        float savedDamage = tag.getFloat("ImpactDamage");
        damage =
                tag.contains("ImpactDamage") && Float.isFinite(savedDamage)
                        ? Math.max(0, Math.min(2048, savedDamage))
                        : 7;
        punch = Math.max(0, Math.min(255, tag.getInt("Punch")));
        flame = tag.getBoolean("Flame");
    }

    @Override
    public net.minecraft.network.protocol.Packet<
                    net.minecraft.network.protocol.game.ClientGamePacketListener>
            getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
}
