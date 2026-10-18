package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.content.*;
import dev.purifiedundead.combat.AbilityPreferences;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public final class RainSixSmoke {
    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("RAIN6_FAILED " + why);
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        var level = server.overworld();
        var data = BlightedRainData.get(level);
        long now = level.getGameTime();
        var pos = new BlockPos(55, 155, 80);
        var states =
                new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
        for (var p : java.util.List.of(pos, pos.below(), pos.east()))
            states.put(p, level.getBlockState(p));
        boolean locked = data.weatherLocked(), enabled = data.enabled();
        var player =
                net.minecraftforge.common.util.FakePlayerFactory.get(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "Rain6Audit"));
        try {
            data.setEnabled(true);
            data.setWeatherLocked(true);
            data.model.reset();
            data.forced = false;
            data.wasSuppressed = false;
            level.setBlockAndUpdate(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(pos, RainItems.BEACON_BLOCK.get().defaultBlockState());
            check(level.getBlockState(pos).getLightEmission() == 4, "inactive beacon light");
            level.setBlockAndUpdate(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            check(level.getBlockState(pos).getValue(StillRainBeaconBlock.ACTIVE), "active state");
            check(level.getBlockState(pos).getLightEmission() == 15, "active beacon light");
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(!level.getLevelData().isRaining(), "beacon clears eternal rain");
            check(RainController.forceStart(level), "forced start under beacon");
            data.model.restore(BlightedRainModel.Phase.ACTIVE.ordinal(), now, 20, now);
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(
                    !data.forced && !level.getLevelData().isRaining(),
                    "forced expiry restores normal weather");
            check(data.model.pausedCooldown() == 18000, "forced cooldown paused");
            check(RainController.forceStart(level), "repeat forced start");
            RainController.stop(level);
            check(!level.getLevelData().isRaining(), "explicit stop restores normal weather");
            check(
                    AbilityPreferences.ferin(player) && AbilityPreferences.movement(player),
                    "preferences default enabled");
            player.getPersistentData().putBoolean(AbilityPreferences.FERIN_DISABLED, true);
            player.getPersistentData().putBoolean(AbilityPreferences.MOVEMENT_DISABLED, true);
            check(
                    !AbilityPreferences.ferin(player) && !AbilityPreferences.movement(player),
                    "server disables abilities");
            var clone =
                    net.minecraftforge.common.util.FakePlayerFactory.get(
                            level,
                            new com.mojang.authlib.GameProfile(
                                    java.util.UUID.randomUUID(), "Rain6Clone"));
            AbilityPreferences.copy(player, clone);
            check(
                    !AbilityPreferences.ferin(clone) && !AbilityPreferences.movement(clone),
                    "preferences survive respawn copy");
            var vals = dev.purifiedundead.config.PurifiedUndeadConfig.VALUES;
            double old = vals.relicFingerDamage.get();
            try {
                vals.relicFingerDamage.set(.7);
                check(
                        Math.abs(
                                        dev.purifiedundead.content.relic.RelicRules
                                                        .damageMultiplier(1, 0, false, 1)
                                                - 1.7)
                                < .00001,
                        "independent config affects damage");
            } finally {
                vals.relicFingerDamage.set(old);
            }
            var fairyType =
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getOptional(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    "touhou_little_maid", "fairy"));
            if (fairyType.isPresent()) {
                var fairy = (net.minecraft.world.entity.LivingEntity) fairyType.get().create(level);
                try {
                    fairy.setPos(55.5, 180, 80.5);
                    fairy.tickCount = 25;
                    data.setBeacon(pos, false);
                    level.setWeatherParameters(0, 6000, true, false);
                    level.setRainLevel(1);
                    data.model.restore(
                            BlightedRainModel.Phase.ACTIVE.ordinal(), now + 6000, 6000, now);
                    check(
                            !fairy.getType().is(RainEffects.AFFECTED),
                            "fairy absent from old vanilla whitelist");
                    check(
                            RainEffects.affected(fairy) && !RainEffects.damageImmune(fairy),
                            "fairy eligible and not immune");
                    check(RainExposure.exposed(fairy), "fairy exposed fixture");
                    float health = fairy.getHealth();
                    RainEffects.tick(fairy);
                    check(fairy.getHealth() < health, "actual fairy rain damage");
                    com.mojang.logging.LogUtils.getLogger()
                            .info(
                                    "RAIN6_FAIRY_OK actual Touhou Little Maid fairy receives rain damage");
                } finally {
                    fairy.discard();
                    data.setBeacon(pos, true);
                }
            }
            com.mojang.logging.LogUtils.getLogger()
                    .info("RAIN6_SERVER_OK forced-expiry/beacon-light/preferences/config");
        } finally {
            for (var e : states.entrySet()) level.setBlockAndUpdate(e.getKey(), e.getValue());
            data.setBeacon(pos, false);
            data.model.reset();
            data.forced = false;
            data.wasSuppressed = false;
            data.setWeatherLocked(locked);
            data.setEnabled(enabled);
            data.setDirty();
        }
    }
}
