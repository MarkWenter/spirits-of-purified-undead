package dev.purifiedundead.blightedrain;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import dev.purifiedundead.entity.BlightedGolemEntity;
import dev.purifiedundead.entity.BlightedKingEntity;

/** Actual server integration checks; no class in this source set is delivered. */
public final class BlightedRainSmoke {
    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("BLIGHTED_RAIN_FAILED: " + why);
    }

    private static <T extends LivingEntity> T mob(
            EntityType<T> type, ServerLevel level, double x, double z) {
        T mob = type.create(level);
        check(mob != null, "create fixture");
        mob.setPos(x, 150, z);
        level.addFreshEntity(mob);
        return mob;
    }

    public static void run(MinecraftServer server) throws Exception {
        var level = server.overworld();
        if (Boolean.getBoolean("purified_undead.rainSnow"))
            check(
                    level.getBiome(new BlockPos(44, 150, 44))
                                    .value()
                                    .getPrecipitationAt(new BlockPos(44, 150, 44))
                            == net.minecraft.world.level.biome.Biome.Precipitation.SNOW,
                    "test world truly uses snow precipitation");
        var data = BlightedRainData.get(level);
        var saved = data.save(new net.minecraft.nbt.CompoundTag());
        var weather = (net.minecraft.world.level.storage.ServerLevelData) level.getLevelData();
        boolean raining = weather.isRaining(), thunder = weather.isThundering();
        int rainTime = weather.getRainTime(),
                thunderTime = weather.getThunderTime(),
                clearTime = weather.getClearWeatherTime();
        float rainLevel = level.getRainLevel(1);
        var fixtures = new java.util.ArrayList<LivingEntity>();
        var source = server.createCommandSourceStack().withPermission(2);
        var dispatcher = server.getCommands().getDispatcher();
        try {
            dispatcher.execute("purifiedundead rain weather vanilla", source);
            data.model.reset();
            level.setWeatherParameters(0, 6000, false, false);
            boolean denied = false;
            try {
                dispatcher.execute("purifiedundead rain start", source);
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
                denied = true;
            }
            check(
                    denied && data.model.phase() == BlightedRainModel.Phase.READY,
                    "clear weather start rejection without mutation");
            denied = false;
            try {
                dispatcher.execute("purifiedundead rain reset", source.withPermission(0));
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
                denied = true;
            }
            check(denied, "admin permission");
            var nether = server.getLevel(net.minecraft.world.level.Level.NETHER);
            if (nether != null) {
                denied = false;
                try {
                    dispatcher.execute("purifiedundead rain start", source.withLevel(nether));
                } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
                    denied = true;
                }
                check(denied, "other dimension rejection");
            }
            level.setWeatherParameters(0, 6000, true, false);
            level.setRainLevel(1);
            check(
                    dispatcher.execute("purifiedundead rain start 1", source) == 1,
                    "registered trigger command");
            long now = level.getGameTime();
            check(data.model.remaining(now) == 300, "fifteen-second warning");
            var roundtrip = BlightedRainData.load(data.save(new net.minecraft.nbt.CompoundTag()));
            var snapshot = roundtrip.save(new net.minecraft.nbt.CompoundTag());
            check(
                    snapshot.getBoolean("weatherLocked") == data.weatherLocked(),
                    "world policy roundtrip");
            var player =
                    new net.minecraftforge.common.util.FakePlayer(
                            level,
                            new com.mojang.authlib.GameProfile(
                                    java.util.UUID.randomUUID(), "RainTest"));
            player.setPos(44, 150, 44);
            player.tickCount = 20;
            fixtures.add(player);
            float health = player.getHealth();
            RainEffects.tick(player);
            check(!player.hasEffect(MobEffects.DIG_SPEED), "warning has no effects");
            data.model.advance(now + 300);
            check(data.model.phase() == BlightedRainModel.Phase.ACTIVE, "active after warning");
            RainEffects.tick(player);
            check(
                    player.hasEffect(MobEffects.DIG_SPEED)
                            && player.hasEffect(MobEffects.DAMAGE_BOOST)
                            && player.getHealth() == health,
                    "player receives effects without damage");
            check(
                    player.getEffect(MobEffects.DIG_SPEED).getAmplifier() == 0
                            && player.getEffect(MobEffects.DIG_SPEED).getDuration() == 200,
                    "level I ten seconds");
            var roof = new BlockPos(44, 153, 44);
            level.setBlockAndUpdate(roof, Blocks.STONE.defaultBlockState());
            check(!RainExposure.exposed(player), "roof shelter");
            level.setBlockAndUpdate(roof, Blocks.AIR.defaultBlockState());
            var absorber = mob(EntityType.COW, level, 43, 47);
            fixtures.add(absorber);
            absorber.setAbsorptionAmount(10);
            absorber.hurt(player.damageSources().playerAttack(player), 1);
            check(
                    absorber.getPersistentData().getBoolean(RainEffects.PLAYER_HIT),
                    "absorbed player damage still counts");
            var immune = mob(EntityType.COW, level, 42, 47);
            fixtures.add(immune);
            immune.setInvulnerable(true);
            immune.hurt(player.damageSources().playerAttack(player), 1);
            check(
                    !immune.getPersistentData().getBoolean(RainEffects.PLAYER_HIT),
                    "invulnerable attack does not count");
            var cow = mob(EntityType.COW, level, 45, 44);
            fixtures.add(cow);
            check(cow.getType().is(RainEffects.AFFECTED), "vanilla affected tag");
            float before = cow.getHealth();
            cow.tickCount = 24;
            RainEffects.tick(cow);
            check(cow.getHealth() == before, "no early damage");
            cow.tickCount = 25;
            RainEffects.tick(cow);
            check(cow.getHealth() == before - 1, "poison-I interval magic damage");
            var wolf = mob(EntityType.WOLF, level, 46, 44);
            fixtures.add(wolf);
            wolf.setTame(true);
            wolf.setOwnerUUID(java.util.UUID.randomUUID());
            before = wolf.getHealth();
            wolf.tickCount = 25;
            RainEffects.tick(wolf);
            check(wolf.getHealth() == before, "offline owner protection");
            var fox = mob(EntityType.FOX, level, 43, 45);
            fixtures.add(fox);
            var foxData = fox.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            var trusted = new net.minecraft.nbt.ListTag();
            trusted.add(net.minecraft.nbt.NbtUtils.createUUID(java.util.UUID.randomUUID()));
            foxData.put("Trusted", trusted);
            fox.load(foxData);
            check(RainOwnership.protectedOwner(fox), "trusted fox owner bridge");
            fox.tickCount = 25;
            float foxHealth = fox.getHealth();
            RainEffects.tick(fox);
            check(fox.getHealth() == foxHealth, "trusted fox immune");
            var rawPlayer =
                    new net.minecraft.server.level.ServerPlayer(
                            server,
                            level,
                            new com.mojang.authlib.GameProfile(
                                    java.util.UUID.randomUUID(), "RawRainImmunity"));
            rawPlayer.connection = player.connection;
            fixtures.add(rawPlayer);
            var spawnField =
                    net.minecraft.server.level.ServerPlayer.class.getDeclaredField(
                            "spawnInvulnerableTime");
            spawnField.setAccessible(true);
            spawnField.setInt(rawPlayer, 0);
            rawPlayer.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
            float rawHealth = rawPlayer.getHealth();
            rawPlayer.hurt(RainEffects.damage(level), 1);
            check(
                    rawPlayer.getHealth() == rawHealth,
                    "rain damage type cannot hurt players even through explicit damage source");
            var ulvHolder =
                    level.registryAccess()
                            .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(dev.purifiedundead.combat.ModDamageTypes.ULV_BLIGHT);
            check(
                    new net.minecraft.world.damagesource.DamageSource(ulvHolder)
                            .is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR),
                    "existing Ulv damage tag preserved");
            var water = mob(EntityType.COD, level, 44, 46);
            fixtures.add(water);
            var waterBlock = new BlockPos(44, 150, 46);
            level.setBlockAndUpdate(waterBlock, Blocks.WATER.defaultBlockState());
            level.setBlockAndUpdate(waterBlock.above(), Blocks.WATER.defaultBlockState());
            check(!RainExposure.exposed(water), "submerged fish");
            level.setBlockAndUpdate(waterBlock, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(waterBlock.above(), Blocks.AIR.defaultBlockState());
            var golem = mob(EntityType.IRON_GOLEM, level, 47, 44);
            fixtures.add(golem);
            golem.setCustomName(net.minecraft.network.chat.Component.literal("RainGolem"));
            for (int i = 1; i <= 60; i++) {
                golem.tickCount = i;
                RainEffects.tick(golem);
            }
            check(
                    golem.getPersistentData().getInt("purified_undead:rain_conversion") == 60,
                    "exposure accumulation");
            var shelter = new BlockPos(47, 154, 44);
            level.setBlockAndUpdate(shelter, Blocks.STONE.defaultBlockState());
            for (int i = 61; i <= 80; i++) {
                golem.tickCount = i;
                RainEffects.tick(golem);
            }
            check(
                    golem.getPersistentData().getInt("purified_undead:rain_conversion") == 40,
                    "one-tick decay under roof");
            level.setBlockAndUpdate(shelter, Blocks.AIR.defaultBlockState());
            for (int i = 81; i <= 240; i++) {
                golem.tickCount = i;
                RainEffects.tick(golem);
            }
            check(golem.isRemoved(), "200-tick golem conversion");
            var converted =
                    level
                            .getEntitiesOfClass(
                                    BlightedGolemEntity.class,
                                    new net.minecraft.world.phys.AABB(46, 149, 43, 49, 155, 46))
                            .stream()
                            .filter(m -> m.getName().getString().equals("RainGolem"))
                            .findFirst()
                            .orElseThrow();
            fixtures.add(converted);
            var evoker = mob(EntityType.EVOKER, level, 48, 44);
            fixtures.add(evoker);
            evoker.getPersistentData().putBoolean(RainEffects.PLAYER_HIT, true);
            for (int i = 1; i <= 200; i++) {
                evoker.tickCount = i;
                RainEffects.tick(evoker);
            }
            check(evoker.isRemoved(), "200-tick evoker conversion");
            var king =
                    level
                            .getEntitiesOfClass(
                                    BlightedKingEntity.class,
                                    new net.minecraft.world.phys.AABB(47, 149, 43, 50, 155, 46))
                            .stream()
                            .findFirst()
                            .orElseThrow();
            fixtures.add(king);
            check(
                    king.getPersistentData().getBoolean(RainEffects.PLAYER_HIT),
                    "conversion retains player-hit history");
            var noPlayer = mob(EntityType.COW, level, 49, 47);
            fixtures.add(noPlayer);
            noPlayer.setHealth(1);
            noPlayer.hurt(RainEffects.damage(level), 1);
            check(!noPlayer.isAlive(), "environment damage can kill");
            check(
                    level.getEntitiesOfClass(ItemEntity.class, noPlayer.getBoundingBox().inflate(1))
                            .isEmpty(),
                    "unclaimed environmental death drops nothing");
            var claimed = mob(EntityType.COW, level, 52, 47);
            fixtures.add(claimed);
            claimed.hurt(player.damageSources().playerAttack(player), 1);
            check(
                    claimed.getPersistentData().getBoolean(RainEffects.PLAYER_HIT),
                    "real successful player damage recorded");
            claimed.invulnerableTime = 0;
            claimed.setHealth(1);
            claimed.hurt(RainEffects.damage(level), 1);
            check(
                    !level.getEntitiesOfClass(ItemEntity.class, claimed.getBoundingBox().inflate(1))
                            .isEmpty(),
                    "player-touched mob retains ordinary loot");
            for (var item :
                    level.getEntitiesOfClass(
                            ItemEntity.class,
                            new net.minecraft.world.phys.AABB(40, 145, 40, 56, 155, 50)))
                item.discard();
            data.model.advance(now + 321);
            check(
                    data.model.phase() == BlightedRainModel.Phase.COOLDOWN
                            && data.model.remaining(now + 321) == 18000,
                    "cooldown starts only after event ends");
            denied = false;
            try {
                dispatcher.execute("purifiedundead rain start", source);
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException expected) {
                denied = true;
            }
            check(denied, "cooldown trigger rejection");
            dispatcher.execute("purifiedundead rain reset", source);
            check(data.model.phase() == BlightedRainModel.Phase.READY, "test reset");
            System.out.println(
                    "BLIGHTED_RAIN_SERVER_OK: command permissions/dimension/clear-weather guards, 15-second warning, saved state, player effects/no damage, roof/water, poison-I damage, owner, accumulation/decay, both conversions, player history, lethal no-loot and cooldown-after-end");
        } finally {
            for (var fixture : fixtures) fixture.discard();
            data.model.restore(
                    saved.getInt("phase"),
                    saved.getLong("deadline"),
                    saved.getInt("duration"),
                    level.getGameTime());
            data.setWeatherLocked(saved.getBoolean("weatherLocked"));
            data.setEnabled(saved.getBoolean("enabled"));
            data.setDirty();
            level.setWeatherParameters(clearTime, rainTime, raining, thunder);
            weather.setThunderTime(thunderTime);
            level.setRainLevel(rainLevel);
        }
    }
}
