package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.mob.*;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.*;

/** Integration checks exercise loaded registries, real conversion/death hooks and injected AI. */
public final class BlightedMobSmoke {
    private static void check(boolean ok, String why) {
        if (!ok) throw new IllegalStateException("BLIGHTED_MOBS_FAILED: " + why);
    }

    private static java.lang.reflect.Field field(Class<?> c, String name) throws Exception {
        var f = c.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static Mob spawn(EntityType<? extends Mob> type, ServerLevel level, double x) {
        level.getChunk(net.minecraft.core.BlockPos.containing(x, 150, 80));
        var m = type.create(level);
        m.setPos(x, 150, 80);
        m.setNoAi(true);
        check(level.addFreshEntity(m), "spawn " + type);
        return m;
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        RainFixSmoke.run(server);
        BlightedTraitsSmoke.run(server);
        RainPartThreeSmoke.run(server);
        RainPartFourSmoke.run(server);
        var level = server.overworld();
        var fixtures = new java.util.ArrayList<Entity>();
        try {
            EntityType<?>[] nativeTypes = {
                EntityType.SPIDER,
                EntityType.ZOMBIE,
                EntityType.SKELETON,
                EntityType.DROWNED,
                EntityType.HUSK,
                EntityType.STRAY,
                EntityType.VILLAGER,
                EntityType.PILLAGER,
                EntityType.VINDICATOR,
                EntityType.WITCH,
                EntityType.PIG,
                EntityType.COW,
                EntityType.SHEEP,
                EntityType.CHICKEN,
                EntityType.HORSE,
                EntityType.DONKEY
            };
            int index = 0;
            for (var type : nativeTypes) {
                var original = spawn((EntityType<? extends Mob>) type, level, 44 + index * .5);
                fixtures.add(original);
                boolean lethal = type == EntityType.VILLAGER || type == EntityType.CHICKEN;
                check(
                        BlightedMobConversions.candidate(original) != lethal,
                        "candidate timing " + type);
                check(
                        BlightedMobConversions.clickable(original)
                                == (!lethal && original instanceof Enemy),
                        "item restriction " + type);
                float max = original.getMaxHealth();
                original.setHealth(max / 2);
                if (original instanceof Zombie z) z.setBaby(true);
                if (original instanceof net.minecraft.world.entity.npc.Villager v) {
                    v.setBaby(true);
                    v.setVillagerData(
                            v.getVillagerData()
                                    .setProfession(
                                            net.minecraft.world.entity.npc.VillagerProfession
                                                    .LIBRARIAN));
                }
                if (original instanceof net.minecraft.world.entity.animal.Sheep s)
                    s.setColor(DyeColor.RED);
                if (original instanceof net.minecraft.world.entity.animal.horse.Horse h) {
                    var coat = h.saveWithoutId(new net.minecraft.nbt.CompoundTag());
                    coat.putInt("Variant", 4 | (3 << 8));
                    h.load(coat);
                }
                Mob next;
                if (lethal) {
                    original.setHealth(1);
                    original.hurt(RainEffects.damage(level), 1);
                    check(original.isRemoved(), "death conversion removes source " + type);
                    next =
                            level
                                    .getEntitiesOfClass(
                                            Mob.class,
                                            original.getBoundingBox().inflate(1),
                                            m ->
                                                    m instanceof BlightedVariant
                                                            && BlightedMobConversions.nativeType(
                                                                            m.getType())
                                                                    == type)
                                    .stream()
                                    .findFirst()
                                    .orElseThrow();
                } else next = BlightedMobConversions.convert(original, level, false);
                check(
                        next != null && next instanceof BlightedVariant,
                        "registered conversion " + type);
                fixtures.add(next);
                check(
                        Math.abs(next.getMaxHealth() - BlightedMobRules.healthFactor(next) * max)
                                < .01,
                        "double max health " + type);
                check(
                        Math.abs(
                                        next.getHealth()
                                                - (lethal
                                                        ? next.getMaxHealth()
                                                        : next.getMaxHealth() / 2))
                                < .01,
                        "double health/full revival " + type);
                check(next.getCustomName() == null, "no generated name " + type);
                check(
                        next.getType().is(BlightedMobRules.BLIGHTED)
                                && RainEffects.damageImmune(next),
                        "blighted tag and immunity " + type);
                check(next.isInvertedHealAndHarm(), "actual undead healing semantics " + type);
                check(
                        BlightedMobConversions.nativeType(next.getType()) == type,
                        "native loot mapping " + type);
                if (next instanceof Zombie z) check(z.isBaby(), "baby zombie preservation " + type);
                if (next instanceof ZombieVillager v)
                    check(
                            v.getVillagerData().getProfession()
                                    == net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN,
                            "villager profession");
                if (next instanceof net.minecraft.world.entity.animal.Sheep s)
                    check(s.getColor() == DyeColor.RED, "sheep color");
                if (next instanceof net.minecraft.world.entity.animal.horse.Horse h)
                    check(
                            h.getVariant() == net.minecraft.world.entity.animal.horse.Variant.BLACK
                                    && h.getMarkings()
                                            == net.minecraft.world.entity.animal.horse.Markings
                                                    .WHITE_DOTS,
                            "horse coat/markings");
                index++;
            }
            var zombie = spawn(EntityType.ZOMBIE, level, 55);
            fixtures.add(zombie);
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            var mount = spawn(EntityType.PIG, level, 55);
            fixtures.add(mount);
            check(zombie.startRiding(mount, true), "rider setup");
            var child = spawn(EntityType.ZOMBIE, level, 55);
            fixtures.add(child);
            check(child.startRiding(zombie, true), "passenger setup");
            var replaced = BlightedMobConversions.convert(zombie, level, false);
            fixtures.add(replaced);
            check(
                    !mount.isVehicle()
                            && !child.isPassenger()
                            && !replaced.isPassenger()
                            && !replaced.isVehicle(),
                    "riders separate");
            check(
                    replaced.getMainHandItem().isEmpty()
                            && replaced.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                            && replaced.canPickUpLoot(),
                    "gear is dropped and can be collected again");
            var items =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            replaced.getBoundingBox().inflate(2));
            check(
                    items.stream()
                                    .filter(
                                            i ->
                                                    i.getItem().is(Items.IRON_SWORD)
                                                            || i.getItem().is(Items.DIAMOND_HELMET))
                                    .mapToInt(i -> i.getItem().getCount())
                                    .sum()
                            == 2,
                    "exact equipment drops");
            items.forEach(Entity::discard);
            var wolf = (net.minecraft.world.entity.animal.Wolf) spawn(EntityType.WOLF, level, 144);
            fixtures.add(wolf);
            wolf.setOwnerUUID(java.util.UUID.randomUUID());
            wolf.setTame(true);
            check(RainEffects.damageImmune(wolf), "owned pet exempt");
            var nativeZombie = spawn(EntityType.ZOMBIE, level, 147);
            fixtures.add(nativeZombie);
            check(RainEffects.damageImmune(nativeZombie), "native undead immune");
            var phantom = spawn(EntityType.PHANTOM, level, 150);
            fixtures.add(phantom);
            check(!RainEffects.damageImmune(phantom), "phantom exception");
            var nameTag = spawn(EntityType.COW, level, 153);
            fixtures.add(nameTag);
            nameTag.setCustomName(net.minecraft.network.chat.Component.literal("NamedCow"));
            var named = BlightedMobConversions.convert(nameTag, level, false);
            fixtures.add(named);
            check(
                    named.getCustomName().getString().equals("NamedCow"),
                    "real player name preserved");
            for (var type :
                    java.util.List.of(
                            dev.purifiedundead.content.ModEntities.BLIGHTED_GOLEM.get(),
                            dev.purifiedundead.content.ModEntities.BLIGHTED_KING.get())) {
                var boss = spawn((EntityType<? extends Mob>) type, level, 55);
                fixtures.add(boss);
                check(
                        boss.getType().is(BlightedMobRules.BLIGHTED)
                                && RainEffects.damageImmune(boss),
                        "old bosses share tag/immunity");
                String key =
                        net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                                .getKey(type)
                                .getPath();
                boss.setCustomName(
                        net.minecraft.network.chat.Component.translatable(
                                "entity.purified_undead." + key));
                var legacy = boss.saveWithoutId(new net.minecraft.nbt.CompoundTag());
                boss.load(legacy);
                check(boss.getCustomName() == null, "remove legacy generated name " + key);
                boss.setCustomName(net.minecraft.network.chat.Component.literal("Keeper"));
                boss.load(boss.saveWithoutId(new net.minecraft.nbt.CompoundTag()));
                check(boss.getCustomName().getString().equals("Keeper"), "keep real boss name tag");
                check(boss.getMobType() == MobType.UNDEAD, "old bosses are undead");
            }
            // Test the transformed AI methods rather than only their numerical helper.
            var vanilla = spawn(EntityType.ZOMBIE, level, 156);
            fixtures.add(vanilla);
            var fast = spawn(BlightedMobContent.ZOMBIE.get(), level, 159);
            fixtures.add(fast);
            var interval = MeleeAttackGoal.class.getDeclaredMethod("getAttackInterval");
            interval.setAccessible(true);
            check(
                    (int) interval.invoke(new MeleeAttackGoal((PathfinderMob) vanilla, 1, false))
                            == 20,
                    "vanilla melee unchanged");
            check(
                    (int) interval.invoke(new MeleeAttackGoal((PathfinderMob) fast, 1, false))
                            == 10,
                    "actual doubled melee");
            var target = spawn(EntityType.COW, level, 180);
            fixtures.add(target);
            int[] delays = new int[2];
            for (int i = 0; i < 2; i++) {
                var caster =
                        (Witch)
                                spawn(
                                        i == 0 ? EntityType.WITCH : BlightedMobContent.WITCH.get(),
                                        level,
                                        170);
                fixtures.add(caster);
                caster.setTarget(target);
                var goal = new RangedAttackGoal(caster, 1, 40, 10);
                check(goal.canUse(), "ranged target");
                goal.tick();
                delays[i] = field(RangedAttackGoal.class, "attackTime").getInt(goal);
            }
            check(delays[0] == 40 && delays[1] == 20, "actual doubled potion/trident cadence");
            var healing = (BlightedWitch) spawn(BlightedMobContent.WITCH.get(), level, 58);
            fixtures.add(healing);
            healing.setHealth(20);
            var potion = new ItemStack(Items.POTION);
            net.minecraft.world.item.alchemy.PotionUtils.setPotion(
                    potion, net.minecraft.world.item.alchemy.Potions.HEALING);
            healing.setItemSlot(EquipmentSlot.MAINHAND, potion);
            healing.setUsingItem(true);
            field(Witch.class, "usingTime").setInt(healing, 0);
            healing.aiStep();
            healing.tick();
            check(healing.getHealth() > 20, "undead witch actually heals instead of self-harm");
            // Verify arrows release at half draw and crossbows really load ammunition, not only
            // advance AI state.
            for (int i = 0; i < 2; i++) {
                var archer =
                        (Skeleton)
                                spawn(
                                        i == 0
                                                ? EntityType.SKELETON
                                                : BlightedMobContent.SKELETON.get(),
                                        level,
                                        170);
                fixtures.add(archer);
                archer.setTarget(target);
                archer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                archer.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
                field(LivingEntity.class, "useItemRemaining")
                        .setInt(archer, archer.getUseItem().getUseDuration() - 10);
                var bow = new RangedBowAttackGoal<Skeleton>(archer, 1, 20, 15);
                field(RangedBowAttackGoal.class, "seeTime").setInt(bow, 10);
                bow.tick();
                check(archer.isUsingItem(), "both bows still drawing at tick ten");
                field(LivingEntity.class, "useItemRemaining")
                        .setInt(archer, archer.getUseItem().getUseDuration() - 29);
                bow.tick();
                check(!archer.isUsingItem(), "both bows release by tick 29");
                if (i == 1)
                    check(
                            field(RangedBowAttackGoal.class, "attackTime").getInt(bow) == 29,
                            "actual bow interval");
                var cross =
                        (Pillager)
                                spawn(
                                        i == 0
                                                ? EntityType.PILLAGER
                                                : BlightedMobContent.PILLAGER.get(),
                                        level,
                                        170);
                fixtures.add(cross);
                cross.setTarget(target);
                cross.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                cross.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
                var goal = new RangedCrossbowAttackGoal<Pillager>(cross, 1, 15);
                var state = field(RangedCrossbowAttackGoal.class, "crossbowState");
                state.set(goal, java.lang.Enum.valueOf((Class) state.getType(), "CHARGING"));
                int charge = CrossbowItem.getChargeDuration(cross.getUseItem());
                field(LivingEntity.class, "useItemRemaining")
                        .setInt(cross, cross.getUseItem().getUseDuration() - (charge + 1) / 2);
                goal.tick();
                check(
                        state.get(goal).toString().equals(i == 0 ? "CHARGING" : "CHARGED"),
                        "actual half crossbow charge");
                if (i == 1) {
                    check(
                            CrossbowItem.isCharged(cross.getMainHandItem()),
                            "real charged crossbow ammunition");
                    int delay = field(RangedCrossbowAttackGoal.class, "attackDelay").getInt(goal);
                    check(delay >= 10 && delay < 20, "actual crossbow delay");
                }
            }
            for (Entity e : fixtures)
                if (e instanceof BlightedVariant) {
                    var m = (Mob) e;
                    Class<?> owner = m.getClass();
                    var sun = owner.getDeclaredMethod("isSunBurnTick");
                    sun.setAccessible(true);
                    check(!(boolean) sun.invoke(m), "sunlight does not ignite " + m.getType());
                }
            var doomed = spawn(BlightedMobContent.COW.get(), level, 58);
            fixtures.add(doomed);
            doomed.hurt(doomed.damageSources().genericKill(), 10000);
            var drops =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            doomed.getBoundingBox().inflate(2));
            check(
                    drops.stream()
                                    .filter(
                                            i ->
                                                    i.getItem()
                                                            .is(
                                                                    dev.purifiedundead.content
                                                                            .ModItems
                                                                            .BLIGHT_FRAGMENT
                                                                            .get()))
                                    .mapToInt(i -> i.getItem().getCount())
                                    .sum()
                            == 1,
                    "independent fragment on animal death");
            drops.forEach(Entity::discard);

            for (boolean sheared : new boolean[] {false, true}) {
                var sheep =
                        (net.minecraft.world.entity.animal.Sheep)
                                spawn(BlightedMobContent.SHEEP.get(), level, 59);
                fixtures.add(sheep);
                sheep.setColor(DyeColor.RED);
                sheep.setSheared(sheared);
                sheep.hurt(sheep.damageSources().genericKill(), 10000);
                var loot =
                        level.getEntitiesOfClass(
                                net.minecraft.world.entity.item.ItemEntity.class,
                                sheep.getBoundingBox().inflate(1));
                check(
                        loot.stream().anyMatch(e -> e.getItem().is(Items.MUTTON)),
                        "native mutton, including sheared sheep");
                check(
                        loot.stream().anyMatch(e -> e.getItem().is(Items.RED_WOOL)) != sheared,
                        "native sheep wool color/shearing");
                loot.forEach(Entity::discard);
            }
            var boosted = spawn(EntityType.ZOMBIE, level, 56);
            fixtures.add(boosted);
            boosted.getAttribute(Attributes.MAX_HEALTH)
                    .addPermanentModifier(
                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                    java.util.UUID.randomUUID(),
                                    "health_test",
                                    6,
                                    net.minecraft.world.entity.ai.attributes.AttributeModifier
                                            .Operation.ADDITION));
            float boostedMax = boosted.getMaxHealth();
            boosted.setHealth(boostedMax / 2);
            var boostedNext = BlightedMobConversions.convert(boosted, level, false);
            fixtures.add(boostedNext);
            check(
                    boostedNext.getMaxHealth() == boostedMax * 1.5
                            && boostedNext.getHealth() == boostedMax * .75,
                    "modified health scales exactly once");
            var horse = spawn(EntityType.HORSE, level, 57);
            fixtures.add(horse);
            var horseData = horse.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            horseData.put(
                    "SaddleItem",
                    new ItemStack(Items.SADDLE).save(new net.minecraft.nbt.CompoundTag()));
            horseData.put(
                    "ArmorItem",
                    new ItemStack(Items.DIAMOND_HORSE_ARMOR)
                            .save(new net.minecraft.nbt.CompoundTag()));
            horse.load(horseData);
            var bare = BlightedMobConversions.convert(horse, level, false);
            fixtures.add(bare);
            check(
                    !((net.minecraft.world.entity.animal.horse.AbstractHorse) bare).isSaddled(),
                    "saddle detaches");
            check(
                    !bare.saveWithoutId(new net.minecraft.nbt.CompoundTag()).contains("ArmorItem"),
                    "horse inventory does not retain armor copy");
            var horseDrops =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            bare.getBoundingBox().inflate(1));
            check(
                    horseDrops.stream()
                                    .filter(
                                            i ->
                                                    i.getItem().is(Items.SADDLE)
                                                            || i.getItem()
                                                                    .is(Items.DIAMOND_HORSE_ARMOR))
                                    .mapToInt(i -> i.getItem().getCount())
                                    .sum()
                            == 2,
                    "exact saddle/armor drops");
            horseDrops.forEach(Entity::discard);
            var saddled = spawn(EntityType.PIG, level, 57);
            fixtures.add(saddled);
            var pigData = saddled.saveWithoutId(new net.minecraft.nbt.CompoundTag());
            pigData.putBoolean("Saddle", true);
            saddled.load(pigData);
            check(
                    ((net.minecraft.world.entity.animal.Pig) saddled).isSaddled(),
                    "pig saddle fixture");
            var pigNext = BlightedMobConversions.convert(saddled, level, false);
            fixtures.add(pigNext);
            check(
                    !((net.minecraft.world.entity.animal.Pig) pigNext).isSaddled(),
                    "pig saddle detaches");
            var pigDrops =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            pigNext.getBoundingBox().inflate(1));
            check(
                    pigDrops.stream()
                                    .filter(e -> e.getItem().is(Items.SADDLE))
                                    .mapToInt(e -> e.getItem().getCount())
                                    .sum()
                            == 1,
                    "exact pig saddle drop");
            pigDrops.forEach(Entity::discard);
            boolean mobLoot =
                    level.getGameRules()
                            .getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT);
            try {
                level.getGameRules()
                        .getRule(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)
                        .set(false, server);
                var silent = spawn(BlightedMobContent.COW.get(), level, 60);
                fixtures.add(silent);
                silent.hurt(silent.damageSources().genericKill(), 10000);
                check(
                        level.getEntitiesOfClass(
                                        net.minecraft.world.entity.item.ItemEntity.class,
                                        silent.getBoundingBox().inflate(1))
                                .isEmpty(),
                        "doMobLoot disables additional fragment");
            } finally {
                level.getGameRules()
                        .getRule(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)
                        .set(mobLoot, server);
            }
            var rainState = BlightedRainData.get(level).model;
            int phase = rainState.phase().ordinal(), duration = rainState.duration();
            long deadline = rainState.deadline(), now = level.getGameTime();
            boolean raining = level.getLevelData().isRaining();
            float rain = level.getRainLevel(1);
            try {
                rainState.reset();
                rainState.start(now - 301, 1200);
                rainState.advance(now);
                level.setWeatherParameters(0, 12000, true, false);
                level.setRainLevel(1);
                var wet = spawn(EntityType.ZOMBIE, level, 58);
                fixtures.add(wet);
                float hp = wet.getHealth();
                for (int i = 1; i < 200; i++) {
                    wet.tickCount = i;
                    RainEffects.tick(wet);
                }
                check(
                        !wet.isRemoved() && wet.getHealth() == hp,
                        "undead rain immunity still accumulates 199 ticks");
                wet.tickCount = 200;
                RainEffects.tick(wet);
                check(wet.isRemoved(), "new undead source transforms at 200 ticks");
                for (var m :
                        level.getEntitiesOfClass(
                                BlightedZombie.class, wet.getBoundingBox().inflate(1)))
                    fixtures.add(m);
            } finally {
                rainState.restore(phase, deadline, duration, now);
                level.setWeatherParameters(0, 12000, raining, false);
                level.setRainLevel(rain);
            }
            System.out.println(
                    "BLIGHTED_MOBS_SERVER_OK: "
                            + nativeTypes.length
                            + " variants, death conversions, native state, variant health,"
                            + " equipment, riders, immunity, actual AI timing and independent"
                            + " fragment");
        } finally {
            fixtures.forEach(Entity::discard);
        }
    }
}
