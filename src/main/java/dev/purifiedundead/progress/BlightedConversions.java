package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.network.chat.Component;

/** Shared conversion transaction. Discard only after replacement insertion succeeds. */
public final class BlightedConversions {
    private BlightedConversions() {}

    public static LivingEntity convert(LivingEntity original, ServerLevel level) {
        if (!original.isAlive() || original.isRemoved()) return null;
        LivingEntity converted;
        if (original instanceof IronGolem golem && !(original instanceof BlightedGolemEntity)) {
            var next = ModEntities.BLIGHTED_GOLEM.get().create(level);
            if (next == null) return null;
            next.moveTo(
                    original.getX(),
                    original.getY(),
                    original.getZ(),
                    original.getYRot(),
                    original.getXRot());
            next.setPlayerCreated(golem.isPlayerCreated());
            next.setNoAi(golem.isNoAi());
            next.setInvulnerable(golem.isInvulnerable());
            next.setHealth(
                    BlightedHealth.rescale(
                            golem.getHealth(), golem.getMaxHealth(), next.getMaxHealth()));
            if (golem.hasCustomName()) {
                next.setCustomName(golem.getCustomName());
                next.setCustomNameVisible(golem.isCustomNameVisible());
            } else
                next.setCustomName(Component.translatable("entity.purified_undead.blighted_golem"));
            converted = next;
        } else if (original instanceof Evoker evoker && !(original instanceof BlightedKingEntity)) {
            converted = JuliusAcquisitionEvents.convert(evoker, level);
            if (converted == null) return null;
        } else return null;
        converted
                .getPersistentData()
                .putBoolean(
                        dev.purifiedundead.blightedrain.RainEffects.PLAYER_HIT,
                        original.getPersistentData()
                                .getBoolean(
                                        dev.purifiedundead.blightedrain.RainEffects.PLAYER_HIT));
        if (!level.addFreshEntity(converted)) return null;
        original.discard();
        level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SOUL,
                converted.getX(),
                converted.getY() + 1,
                converted.getZ(),
                24,
                .55,
                .8,
                .55,
                .04);
        return converted;
    }
}
