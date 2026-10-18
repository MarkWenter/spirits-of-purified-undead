package dev.purifiedundead.blightedrain;

import dev.purifiedundead.content.ModEffects;
import dev.purifiedundead.entity.BlightedGolemEntity;
import dev.purifiedundead.entity.BlightedKingEntity;
import dev.purifiedundead.progress.BlightedConversions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

/** Server authority for environmental effects; no client decides damage or conversion. */
public final class RainEffects {
    public static final String PLAYER_HIT = "purified_undead:rain_player_hit";
    private static final String PROGRESS = "purified_undead:rain_conversion",
            LAST = "purified_undead:rain_conversion_time";
    public static final ResourceKey<DamageType> DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, id("blighted_rain"));
    public static final TagKey<EntityType<?>> AFFECTED =
            TagKey.create(Registries.ENTITY_TYPE, id("blighted_rain_affected"));
    public static final TagKey<EntityType<?>> IMMUNE =
            TagKey.create(Registries.ENTITY_TYPE, id("blighted_rain_immune"));

    private RainEffects() {}

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath("purified_undead", name);
    }

    public static DamageSource damage(ServerLevel level) {
        return new DamageSource(
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DAMAGE));
    }

    public static boolean affected(LivingEntity mob) {
        return mob instanceof net.minecraft.world.entity.Mob || mob.getType().is(AFFECTED);
    }

    public static boolean protectedMob(LivingEntity mob) {
        return mob.getType().is(IMMUNE)
                || RainImmunity.contains(mob.getType())
                || RainOwnership.protectedOwner(mob);
    }

    public static boolean damageImmune(LivingEntity mob) {
        return protectedMob(mob)
                || (mob.getType() != EntityType.PHANTOM
                        && (mob.getMobType() == net.minecraft.world.entity.MobType.UNDEAD
                                || mob.getType()
                                        .is(
                                                dev.purifiedundead.blightedrain.mob.BlightedMobRules
                                                        .BLIGHTED)));
    }

    public static void tick(LivingEntity mob) {
        if (!(mob.level() instanceof ServerLevel level)
                || level.dimension() != net.minecraft.world.level.Level.OVERWORLD
                || !mob.isAlive()) return;
        var state = BlightedRainData.get(level).model;
        if (mob instanceof net.minecraft.world.entity.npc.Villager villager)
            RainShelter.tick(villager, state.occupied());
        boolean active = state.phase() == BlightedRainModel.Phase.ACTIVE;
        if (mob instanceof ServerPlayer player) {
            if (active
                    && !player.isSpectator()
                    && player.tickCount % 20 == 0
                    && RainExposure.exposed(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
                player.addEffect(
                        new MobEffectInstance(ModEffects.BLIGHTED_TRANSFORMATION.get(), 200, 0));
            }
            return;
        }
        boolean convertible =
                (mob instanceof IronGolem && !(mob instanceof BlightedGolemEntity))
                        || (mob instanceof Evoker && !(mob instanceof BlightedKingEntity));
        convertible =
                (convertible
                                || (mob instanceof net.minecraft.world.entity.Mob m
                                        && dev.purifiedundead.blightedrain.mob
                                                .BlightedMobConversions.candidate(m)))
                        && affected(mob);
        var tag = mob.getPersistentData();
        int progress = convertible ? tag.getInt(PROGRESS) : 0;
        if (!active && progress == 0) return;
        if (protectedMob(mob)) {
            if (progress != 0) tag.remove(PROGRESS);
            return;
        }
        boolean exposed =
                active
                        && (convertible || mob.tickCount % BlightedRainModel.DAMAGE_INTERVAL == 0)
                        && RainExposure.exposed(mob);
        if (convertible) {
            long now = level.getGameTime(), last = tag.contains(LAST) ? tag.getLong(LAST) : now - 1;
            // No exposure accrues in unloaded chunks; skipped ticks decay the old residue.
            progress = BlightedRainModel.progress(progress, exposed, Math.max(0, now - last - 1));
            tag.putLong(LAST, now);
            tag.putInt(PROGRESS, progress);
            if (progress >= BlightedRainModel.CONVERSION_TICKS
                    && now >= tag.getLong("purified_undead:rain_retry")) {
                if (BlightedConversions.convert(mob, level) != null) return;
                tag.putLong("purified_undead:rain_retry", now + 20);
            }
        }
        if (exposed
                && !damageImmune(mob)
                && affected(mob)
                && mob.tickCount % BlightedRainModel.DAMAGE_INTERVAL == 0)
            mob.hurt(damage(level), 1F);
    }
}
