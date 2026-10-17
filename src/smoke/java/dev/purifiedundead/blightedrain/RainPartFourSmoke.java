package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.content.*;
import dev.purifiedundead.blightedrain.mob.*;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.progress.*;
import dev.purifiedundead.purification.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.core.*;
import net.minecraft.world.level.block.Blocks;

public final class RainPartFourSmoke {
    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("RAIN_PART4_FAILED " + why);
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        var level = server.overworld();
        var player =
                net.minecraftforge.common.util.FakePlayerFactory.get(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "RainPart4Audit"));
        player.setPos(55.5, 150, 80.5);
        var pos = new BlockPos(55, 150, 80);
        level.getChunk(pos);
        var oldFloor = level.getBlockState(pos.below());
        var oldPlant = level.getBlockState(pos);
        boolean oldRule = level.getGameRules().getBoolean(RainWorldOptions.ETERNAL_RAIN);
        boolean oldLocked = BlightedRainData.get(level).weatherLocked();
        var entities = new java.util.ArrayList<Entity>();
        try {
            // Real world creation serializes the gamerule; initialize a fresh world state from it.
            var create =
                    BlightedRainData.class.getDeclaredMethod(
                            "create", net.minecraft.server.level.ServerLevel.class);
            create.setAccessible(true);
            level.getGameRules().getRule(RainWorldOptions.ETERNAL_RAIN).set(false, server);
            var fresh = (BlightedRainData) create.invoke(null, level);
            check(!fresh.weatherLocked(), "world option disables eternal rain");
            fresh.welcome(player);
            fresh.welcome(player);
            check(
                    player.getInventory().countItem(RainItems.EMBLEM.get()) == 1,
                    "one first-entry emblem");
            var restored = BlightedRainData.load(fresh.save(new CompoundTag()));
            restored.welcome(player);
            check(
                    player.getInventory().countItem(RainItems.EMBLEM.get()) == 1,
                    "gift ledger survives reload");
            BlightedRainData.load(new CompoundTag()).welcome(player);
            check(
                    player.getInventory().countItem(RainItems.EMBLEM.get()) == 1,
                    "old worlds not retroactively gifted");
            level.getGameRules().getRule(RainWorldOptions.ETERNAL_RAIN).set(true, server);
            var normal = (BlightedRainData) create.invoke(null, level);
            check(normal.weatherLocked(), "enabled default retained");
            normal.welcome(player);
            check(
                    player.getInventory().countItem(RainItems.EMBLEM.get()) == 1,
                    "enabled world no gift");
            for (var item :
                    java.util.List.of(ModItems.WHITE_LOTUS.get(), ModItems.SCARLET_LOTUS.get())) {
                level.setBlockAndUpdate(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState());
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                player.setItemInHand(
                        net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(item, 2));
                var hit =
                        new net.minecraft.world.phys.BlockHitResult(
                                net.minecraft.world.phys.Vec3.atCenterOf(pos.below()),
                                Direction.UP,
                                pos.below(),
                                false);
                var result =
                        item.useOn(
                                new net.minecraft.world.item.context.UseOnContext(
                                        player,
                                        net.minecraft.world.InteractionHand.MAIN_HAND,
                                        hit));
                check(result.consumesAction(), "lotus places from existing item");
                var planted = level.getBlockState(pos);
                check(
                        planted.getBlock() instanceof LotusBlock
                                && planted.getBlock().asItem() == item,
                        "plant retains original item identity");
                check(player.getMainHandItem().getCount() == 1, "placement consumes one");
                var drops =
                        net.minecraft.world.level.block.Block.getDrops(planted, level, pos, null);
                check(drops.size() == 1 && drops.get(0).is(item), "plant drops original lotus");
                level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                check(!planted.canSurvive(level, pos), "plant rejects stone");
            }
            player.setHealth(2);
            var white = new ItemStack(ModItems.WHITE_LOTUS.get(), 2);
            ModItems.WHITE_LOTUS.get().finishUsingItem(white, level, player);
            check(
                    player.getHealth() == 12 && white.getCount() == 1,
                    "white lotus still heals half maximum health");
            var scarlet = new ItemStack(ModItems.SCARLET_LOTUS.get(), 2);
            ModItems.SCARLET_LOTUS.get().finishUsingItem(scarlet, level, player);
            check(
                    player.hasEffect(MobEffects.DAMAGE_BOOST)
                            && player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() == 3
                            && scarlet.getCount() == 1,
                    "scarlet lotus consumption retained");
            player.removeAllEffects();
            player.setItemInHand(
                    net.minecraft.world.InteractionHand.OFF_HAND,
                    new ItemStack(ModItems.TIRED_HEART.get()));
            var eleine = new EleineAcquisitionEvents();
            for (var type :
                    java.util.List.of(
                            EntityType.DROWNED,
                            BlightedMobContent.DROWNED.get(),
                            BlightedMobContent.WITCH.get())) {
                var mob = type.create(level);
                entities.add(mob);
                eleine.onLivingDeath(
                        new net.minecraftforge.event.entity.living.LivingDeathEvent(
                                mob, level.damageSources().playerAttack(player)));
            }
            check(
                    player.getInventory().countItem(ModItems.ELEINE_WARRIOR.get()) == 1,
                    "mixed three-kill reward");
            check(player.getOffhandItem().isEmpty(), "tired heart consumed once");
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
            var hoenir = new HoenirAcquisitionEvents();
            var zombie = EntityType.ZOMBIE.create(level);
            entities.add(zombie);
            hoenir.onLivingDeath(
                    new net.minecraftforge.event.entity.living.LivingDeathEvent(
                            zombie, level.damageSources().playerAttack(player)));
            check(
                    player.getInventory().countItem(ModItems.HOENIR_WARRIOR.get()) == 0,
                    "ordinary zombie no longer qualifies");
            var pig = BlightedMobContent.PIG.get().create(level);
            entities.add(pig);
            hoenir.onLivingDeath(
                    new net.minecraftforge.event.entity.living.LivingDeathEvent(
                            pig, level.damageSources().playerAttack(player)));
            check(
                    player.getInventory().countItem(ModItems.HOENIR_WARRIOR.get()) == 1,
                    "tagged blighted passive mob qualifies");
            var villager = BlightedMobContent.VILLAGER.get().create(level);
            entities.add(villager);
            villager.setPos(55.5, 150, 80.5);
            villager.setNoAi(true);
            level.addFreshEntity(villager);
            player.setItemInHand(
                    net.minecraft.world.InteractionHand.MAIN_HAND,
                    PureElixirBrewing.stack(
                            Items.POTION, dev.purifiedundead.content.ModPotions.PURE_ELIXIR.get()));
            villager.mobInteract(player, net.minecraft.world.InteractionHand.MAIN_HAND);
            var tag = villager.saveWithoutId(new CompoundTag());
            tag.putInt("ConversionTime", 1);
            villager.load(tag);
            villager.tick();
            check(villager.isRemoved(), "real blighted villager cure finishes");
            // Fake player is offline to the player list; exercise the existing pending cure ledger.
            new FadenAcquisitionEvents()
                    .onLogin(
                            new net.minecraftforge.event.entity.player.PlayerEvent
                                    .PlayerLoggedInEvent(player));
            check(
                    player.getInventory().countItem(ModItems.FADEN_WARRIOR.get()) == 1,
                    "blighted cure grants Faden on login");
            new FadenAcquisitionEvents()
                    .onLogin(
                            new net.minecraftforge.event.entity.player.PlayerEvent
                                    .PlayerLoggedInEvent(player));
            check(
                    player.getInventory().countItem(ModItems.FADEN_WARRIOR.get()) == 1,
                    "Faden reward not duplicated");
            System.out.println(
                    "RAIN_PART4_SERVER_OK world-options/gift-persistence/lotus-placement-drops/mixed-Eleine/tagged-Hoenir/real-Faden-cure");
        } finally {
            entities.forEach(Entity::discard);
            player.discard();
            level.setBlockAndUpdate(pos.below(), oldFloor);
            level.setBlockAndUpdate(pos, oldPlant);
            level.getGameRules().getRule(RainWorldOptions.ETERNAL_RAIN).set(oldRule, server);
            BlightedRainData.get(level).setWeatherLocked(oldLocked);
        }
    }
}
