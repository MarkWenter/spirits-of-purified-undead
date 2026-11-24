package dev.purifiedundead.progress;

import dev.purifiedundead.blightedrain.RainEffects;
import dev.purifiedundead.blightedrain.mob.BlightedMobConversions;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.entity.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/** Shared conversion transaction. Discard only after replacement insertion succeeds. */
public final class BlightedConversions {
    private BlightedConversions() {}

    /** A positive client result consumes the entity interaction before the held spirit starts purification. */
    public static boolean useSpirit(Player player, Mob target, ItemStack stack) {
        if (!stack.is(ModItems.BLIGHTED_SPIRIT.get()) || player.isSpectator() || !player.isAlive()
                || !target.isAlive() || player.level() != target.level()) return false;

        // Curios' predicate lookup reads current active slots even after a same-tick equipment change.
        boolean equipped = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(item -> item.is(ModItems.ANCIENT_CONTRACT.get())))
                .orElse(false);
        if (!equipped || !acceptsSpirit(target)) return false;
        if (target.level().isClientSide()) return true;
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        LivingEntity converted = convert(target, serverPlayer.serverLevel());
        if (converted == null) return false;
        if (!player.getAbilities().instabuild) stack.shrink(1);
        if (converted instanceof BlightedGolemEntity) {
            player.displayClientMessage(Component.translatable("message.purified_undead.groth.transformed"), false);
        } else if (converted instanceof BlightedKingEntity) {
            player.displayClientMessage(Component.translatable("message.purified_undead.julius.transformed"), false);
        }
        return true;
    }

    private static boolean acceptsSpirit(Mob mob) {
        boolean client = mob.level().isClientSide();
        if (mob instanceof IronGolem) {
            return !(mob instanceof BlightedGolemEntity)
                    && (client || PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothAcquisitionEnabled));
        }
        if (mob instanceof Evoker evoker) {
            return !(mob instanceof BlightedKingEntity)
                    && (client || (PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.juliusAcquisitionEnabled)
                            && !evoker.getPersistentData().getBoolean(JuliusAcquisitionEvents.BLIGHTED_KING_KEY)));
        }
        if (!BlightedMobConversions.clickable(mob)) return false;
        if (client) {
            // Entity type tags and tame flags are synchronized. Server config and owner adapters are not.
            return !mob.getType().is(RainEffects.IMMUNE) && !(mob instanceof AbstractHorse horse && horse.isTamed());
        }
        return !RainEffects.protectedMob(mob);
    }

    public static LivingEntity convert(LivingEntity original, ServerLevel level) {
        try {
            return convertPrepared(original, level);
        } catch (RuntimeException | LinkageError failure) {
            if (original.isRemoved()) throw failure;
            dev.purifiedundead.util.FailureIsolation.report(
                    "entity conversion preparation", failure);
            return null;
        }
    }

    private static LivingEntity convertPrepared(LivingEntity original, ServerLevel level) {
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
            }
            converted = next;
        } else if (original instanceof Evoker evoker && !(original instanceof BlightedKingEntity)) {
            converted = JuliusAcquisitionEvents.convert(evoker, level);
            if (converted == null) return null;
        } else if (original instanceof net.minecraft.world.entity.Mob mob)
            return dev.purifiedundead.blightedrain.mob.BlightedMobConversions.convert(
                    mob, level, false);
        else return null;
        converted
                .getPersistentData()
                .putBoolean(
                        dev.purifiedundead.blightedrain.RainEffects.PLAYER_HIT,
                        original.getPersistentData()
                                .getBoolean(
                                        dev.purifiedundead.blightedrain.RainEffects.PLAYER_HIT));
        for (var slot : net.minecraft.world.entity.EquipmentSlot.values())
            converted.setItemSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
        if (!dev.purifiedundead.progress.ConversionCommit.replace(
                original, converted, level, java.util.List.of())) return null;
        dev.purifiedundead.util.FailureIsolation.run(
                "conversion particles",
                () -> {
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
                });
        return converted;
    }
}
