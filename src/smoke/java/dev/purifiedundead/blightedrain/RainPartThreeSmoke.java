package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.content.*;
import dev.purifiedundead.blightedrain.mob.*;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

public final class RainPartThreeSmoke {
    private static void check(boolean value, String name) {
        if (!value) throw new IllegalStateException("RAIN_PART3_FAILED: " + name);
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        var level = server.overworld();
        var data = BlightedRainData.get(level);
        long now = level.getGameTime();
        var player =
                net.minecraftforge.common.util.FakePlayerFactory.get(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "RainPart3Audit"));
        player.setPos(55.5, 150, 80.5);
        var pos = new BlockPos(55, 155, 80);
        var states =
                new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
        for (var p : java.util.List.of(pos, pos.below(), pos.east()))
            states.put(p, level.getBlockState(p));
        boolean oldLocked = data.weatherLocked(), oldEnabled = data.enabled();
        var entities = new java.util.ArrayList<Entity>();
        try {
            data.setEnabled(true);
            data.setWeatherLocked(true);
            data.model.reset();
            data.forced = false;
            level.setBlockAndUpdate(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState());
            level.setBlockAndUpdate(pos, RainItems.BEACON_BLOCK.get().defaultBlockState());
            check(!StillRainBeaconBlock.isActive(level, pos), "beacon needs redstone");
            level.setBlockAndUpdate(pos.east(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            check(
                    StillRainBeaconBlock.isActive(level, pos) && data.suppressed(),
                    "powered amethyst beacon activates");
            level.setWeatherParameters(0, 6000, true, false);
            data.model.start(now, 6000);
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(
                    data.model.phase() == BlightedRainModel.Phase.COOLDOWN
                            && data.model.pausedCooldown() == 18000,
                    "beacon ends natural event and pauses full cooldown");
            var snapshot = data.save(new CompoundTag());
            var restored = BlightedRainData.load(snapshot);
            check(restored.suppressed(), "beacon activation persisted");
            var remote = new BlockPos(1000000, 150, 1000000);
            check(!level.hasChunkAt(remote), "unloaded beacon fixture");
            data.setBeacon(remote, true);
            data.validateBeacons(level);
            data.setBeacon(pos, false);
            check(data.suppressed(), "unloaded beacon retains activation without ticket");
            data.setBeacon(remote, false);
            data.setBeacon(pos, true);
            level.setWeatherParameters(0, 6000, false, false);
            check(
                    RainController.forceStart(level),
                    "emblem overrides beacon and cooldown in clear weather");
            check(level.getLevelData().isRaining() && data.forced, "forced rain established");
            check(
                    !RainController.forceStart(level),
                    "occupied event cannot consume duplicate charge");
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(data.model.occupied(), "beacon preserves forced event");
            data.model.advance(now + 300);
            data.model.advance(now + 300 + data.model.duration());
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(data.model.pausedCooldown() >= 0, "forced event cooldown pauses under beacon");
            level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());
            data.lastProcessedTime = Long.MIN_VALUE;
            RainController.tick(level);
            check(
                    !data.suppressed() && data.model.pausedCooldown() < 0,
                    "power removal resumes cooldown");
            var spider = BlightedMobContent.SPIDER.get().create(level);
            entities.add(spider);
            check(
                    spider.getMaxHealth() == 24
                            && Math.abs(
                                            spider.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)
                                                    - .24)
                                    < .0001,
                    "spider attributes");
            spider.getAttribute(Attributes.MAX_HEALTH).setBaseValue(32);
            spider.setHealth(16);
            spider.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);
            var saved = spider.saveWithoutId(new CompoundTag());
            saved.remove("BlightedSpiderTraits");
            spider.load(saved);
            check(
                    spider.getMaxHealth() == 24 && spider.getHealth() == 12,
                    "old spider health migrates proportionally");
            saved = spider.saveWithoutId(new CompoundTag());
            spider.load(saved);
            check(spider.getHealth() == 12, "spider migration idempotent");
            var target = EntityType.COW.create(level);
            entities.add(target);
            target.setPos(55.5, 150, 80.5);
            target.setNoAi(true);
            level.addFreshEntity(target);
            int weakness = 0;
            spider.getRandom().setSeed(92847);
            for (int i = 0; i < 1000; i++) {
                target.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);
                BlightedMobRules.afterDamage(target, spider.damageSources().mobAttack(spider), 1);
                if (target.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)) weakness++;
            }
            check(weakness > 60 && weakness < 140, "spider weakness approximately ten percent");
            var shield = new ItemStack(RainItems.SHIELD.get());
            double armor = player.getAttributeValue(Attributes.ARMOR);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, shield);
            RainEquipment.tick(player);
            check(
                    player.getAttributeValue(Attributes.ARMOR) == armor + 5,
                    "shield grants five armor");
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, shield.copy());
            RainEquipment.tick(player);
            check(
                    player.getAttributeValue(Attributes.ARMOR) == armor + 5,
                    "two shields do not duplicate armor");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            RainEquipment.tick(player);
            check(
                    player.getAttributeValue(Attributes.ARMOR) == armor,
                    "armor removed after unequip");
            var youth = new ItemStack(RainItems.YOUTH.get());
            youth.setDamageValue(10);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, youth);
            check(
                    !youth.getItem()
                            .use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND)
                            .getResult()
                            .consumesAction(),
                    "exhausted youth cannot throw");
            for (int i = 0; i < 599; i++)
                youth.getItem().inventoryTick(youth, level, player, 0, true);
            check(youth.getDamageValue() == 10 && youth.getCount() == 1, "exhausted item survives");
            youth.getItem().inventoryTick(youth, level, player, 0, true);
            check(youth.getDamageValue() == 9, "thirty-second recovery");
            youth.getItem().use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND);
            check(
                    youth.getDamageValue() == 10 && youth.getCount() == 1,
                    "last charge throws without destroying item");
            player.getAttribute(
                            net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(
                                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                            "attributeslib", "crit_chance")))
                    .setBaseValue(0);
            target.setHealth(target.getMaxHealth());
            target.invulnerableTime = 0;
            var projectile = new CliffYouthProjectile(RainItems.YOUTH_PROJECTILE.get(), level);
            entities.add(projectile);
            projectile.configure(player, new ItemStack(RainItems.YOUTH.get()));
            var hit =
                    CliffYouthProjectile.class.getDeclaredMethod(
                            "onHit", net.minecraft.world.phys.HitResult.class);
            hit.setAccessible(true);
            hit.invoke(projectile, new net.minecraft.world.phys.EntityHitResult(target));
            check(
                    target.hasEffect(dev.purifiedundead.content.ModEffects.STUNNED.get()),
                    "impact stuns target");
            check(
                    Math.abs(target.getHealth() - (target.getMaxHealth() - 7)) < .01,
                    "impact deals exactly seven damage");
            var enchanted = new ItemStack(RainItems.YOUTH.get());
            enchanted.enchant(net.minecraft.world.item.enchantment.Enchantments.POWER_ARROWS, 3);
            enchanted.enchant(net.minecraft.world.item.enchantment.Enchantments.FLAMING_ARROWS, 1);
            enchanted.enchant(net.minecraft.world.item.enchantment.Enchantments.PUNCH_ARROWS, 2);
            var enchantedShot = new CliffYouthProjectile(RainItems.YOUTH_PROJECTILE.get(), level);
            entities.add(enchantedShot);
            enchantedShot.configure(player, enchanted);
            var shotNbt = enchantedShot.saveWithoutId(new CompoundTag());
            check(
                    shotNbt.getFloat("ImpactDamage") == 9
                            && shotNbt.getInt("Punch") == 2
                            && shotNbt.getBoolean("Flame"),
                    "bow enchants snapshot");
            target.invulnerableTime = 0;
            target.setHealth(target.getMaxHealth());
            hit.invoke(enchantedShot, new net.minecraft.world.phys.EntityHitResult(target));
            check(
                    Math.abs(target.getHealth() - (target.getMaxHealth() - 9)) < .01
                            && target.isOnFire(),
                    "power and flame impact");
            check(
                    !enchanted
                            .getItem()
                            .canApplyAtEnchantingTable(
                                    enchanted,
                                    net.minecraft.world.item.enchantment.Enchantments
                                            .INFINITY_ARROWS),
                    "infinity excluded");

            var cow = BlightedMobContent.COW.get().create(level);
            entities.add(cow);
            var drops = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
            drops.add(
                    new net.minecraft.world.entity.item.ItemEntity(
                            level, 55, 150, 80, new ItemStack(Items.COOKED_BEEF, 5)));
            RainDrops.add(
                    new net.minecraftforge.event.entity.living.LivingDropsEvent(
                            cow, cow.damageSources().generic(), drops, 0, false));
            check(
                    drops.stream()
                            .anyMatch(
                                    d ->
                                            d.getItem().is(RainItems.MEAT.get())
                                                    && d.getItem().getCount() == 5),
                    "blighted meat follows native cooked meat count");
            int dust = 0;
            spider.getRandom().setSeed(92847);
            for (int i = 0; i < 1000; i++) {
                drops.clear();
                RainDrops.add(
                        new net.minecraftforge.event.entity.living.LivingDropsEvent(
                                spider, spider.damageSources().generic(), drops, 0, false));
                if (!drops.isEmpty()) {
                    dust++;
                    check(
                            drops.get(0).getItem().getCount() >= 1
                                    && drops.get(0).getItem().getCount() <= 4,
                            "dust base count");
                }
            }
            check(dust > 390 && dust < 510, "dust approximately forty-five percent");
            var zombie = BlightedMobContent.ZOMBIE.get().create(level);
            entities.add(zombie);
            zombie.getRandom().setSeed(92847);
            int emblems = 0;
            for (int i = 0; i < 10000; i++) {
                drops.clear();
                RainDrops.add(
                        new net.minecraftforge.event.entity.living.LivingDropsEvent(
                                zombie, zombie.damageSources().generic(), drops, 0, false));
                if (!drops.isEmpty()) {
                    emblems++;
                    check(drops.get(0).getItem().getCount() == 1, "emblem capped at one");
                }
            }
            check(emblems > 180 && emblems < 330, "emblem approximately 2.5 percent");
            check(
                    dev.purifiedundead.foundry.FoundryRecipes.all().stream()
                            .anyMatch(
                                    r ->
                                            r.output().equals("purified_undead:still_rain_beacon")
                                                    && !r.consumeTop()),
                    "beacon foundry preserves treasure");
            com.mojang.logging.LogUtils.getLogger()
                    .info(
                            "RAIN_PART3_SERVER_OK"
                                + " beacon/persistence/pause/override/spider/shield/recovery/impact/drops/foundry");
        } finally {
            for (var entity : entities) entity.discard();
            for (var entry : states.entrySet())
                level.setBlockAndUpdate(entry.getKey(), entry.getValue());
            data.setBeacon(pos, false);
            data.model.reset();
            data.forced = false;
            data.setEnabled(oldEnabled);
            data.setWeatherLocked(oldLocked);
            data.setDirty();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            RainEquipment.tick(player);
            for (var e :
                    level.getEntitiesOfClass(
                            CliffYouthProjectile.class, player.getBoundingBox().inflate(20)))
                e.discard();
        }
    }
}
