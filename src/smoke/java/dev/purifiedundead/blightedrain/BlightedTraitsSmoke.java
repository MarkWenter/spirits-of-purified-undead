package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.mob.*;
import dev.purifiedundead.content.ModPotions;
import dev.purifiedundead.progress.PureElixirBrewing;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

public final class BlightedTraitsSmoke {
    private static void check(boolean ok, String what) {
        if (!ok) throw new IllegalStateException("TRAITS_FAILED: " + what);
    }

    public static void run(net.minecraft.server.MinecraftServer server) throws Exception {
        var level = server.overworld();
        var fixtures = new java.util.ArrayList<Entity>();
        var player =
                net.minecraftforge.common.util.FakePlayerFactory.get(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "TraitsAudit"));
        player.setPos(55.5, 150, 80.5);
        try {
            for (var type :
                    java.util.List.of(
                            BlightedMobContent.ZOMBIE.get(),
                            BlightedMobContent.SKELETON.get(),
                            BlightedMobContent.DROWNED.get(),
                            BlightedMobContent.HUSK.get(),
                            BlightedMobContent.STRAY.get(),
                            BlightedMobContent.VILLAGER.get())) {
                var mob = type.create(level);
                fixtures.add(mob);
                mob.setPos(55.5, 150, 80.5);
                mob.setNoAi(true);
                level.addFreshEntity(mob);
                check(mob.getMaxHealth() == 30, "fresh spawn health " + type);
                double expected =
                        mob instanceof BlightedVillager
                                ? .25
                                : mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton
                                        ? .175
                                        : .161;
                check(
                        Math.abs(mob.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - expected)
                                < .0001,
                        "fresh movement " + type);
                mob.setHealth(15);
                var saved = mob.saveWithoutId(new CompoundTag());
                mob.load(saved);
                check(
                        mob.getMaxHealth() == 30 && mob.getHealth() == 15,
                        "reload does not stack traits");
                // Simulate pre-update save, retaining vanilla NBT serialization shape.
                mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
                mob.setHealth(20);
                mob.getAttribute(Attributes.MOVEMENT_SPEED)
                        .setBaseValue(mob instanceof BlightedVillager ? .5 : expected / .7);
                saved = mob.saveWithoutId(new CompoundTag());
                saved.remove("BlightedTraitsVersion");
                mob.load(saved);
                check(mob.getMaxHealth() == 30 && mob.getHealth() == 15, "legacy health migration");
                check(
                        Math.abs(mob.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - expected)
                                < .0001,
                        "legacy movement migration");
                if (!(mob instanceof BlightedVillager)) {
                    var target = EntityType.COW.create(level);
                    fixtures.add(target);
                    target.setPos(55.5, 150, 80.5);
                    target.setNoAi(true);
                    target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
                    target.setHealth(100);
                    level.addFreshEntity(target);
                    mob.getAttribute(
                                    net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES
                                            .getValue(
                                                    net.minecraft.resources.ResourceLocation
                                                            .fromNamespaceAndPath(
                                                                    "attributeslib",
                                                                    "crit_chance")))
                            .setBaseValue(0);
                    check(target.hurt(mob.damageSources().mobAttack(mob), 5), "successful attack");
                    float damage = mob instanceof BlightedHusk ? 7 : 6;
                    check(
                            Math.abs(target.getHealth() - (100 - damage)) < .01,
                            "actual damage scaling " + type + " hp=" + target.getHealth());
                    if (mob instanceof BlightedDrowned)
                        check(
                                target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() == 200,
                                "drowned slow duration");
                    if (mob instanceof BlightedDrowned
                            || mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton) {
                        target.invulnerableTime = 0;
                        target.setHealth(100);
                        target.removeAllEffects();
                        var arrow = net.minecraft.world.entity.EntityType.ARROW.create(level);
                        arrow.setOwner(mob);
                        check(
                                target.hurt(mob.damageSources().arrow(arrow, mob), 5),
                                "projectile hit");
                        check(Math.abs(target.getHealth() - 94) < .01, "projectile damage scaling");
                        if (mob instanceof BlightedDrowned)
                            check(
                                    target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration()
                                            == 200,
                                    "ranged drowned slow");
                        arrow.discard();
                    }
                }
            }
            var village = BlightedMobContent.VILLAGER.get().create(level);
            fixtures.add(village);
            village.setPos(55.5, 150, 80.5);
            village.setNoAi(true);
            level.addFreshEntity(village);
            village.setVillagerData(
                    village.getVillagerData()
                            .setProfession(
                                    net.minecraft.world.entity.npc.VillagerProfession.LIBRARIAN));
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    PureElixirBrewing.stack(Items.SPLASH_POTION, ModPotions.PURE_ELIXIR.get()));
            village.mobInteract(player, InteractionHand.MAIN_HAND);
            check(!village.isConverting(), "splash rejected");
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    PureElixirBrewing.stack(Items.POTION, ModPotions.PURE_ELIXIR.get()));
            village.mobInteract(player, InteractionHand.MAIN_HAND);
            check(village.isConverting(), "drinkable elixir starts cure");
            check(player.getMainHandItem().is(Items.GLASS_BOTTLE), "survival returns bottle");
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    PureElixirBrewing.stack(Items.POTION, ModPotions.PURE_ELIXIR.get()));
            village.mobInteract(player, InteractionHand.MAIN_HAND);
            check(
                    player.getMainHandItem().is(Items.POTION),
                    "repeat click does not consume second potion");
            var data = village.saveWithoutId(new CompoundTag());
            check(
                    data.getInt("ConversionTime") >= 3600 && data.getInt("ConversionTime") <= 6000,
                    "vanilla cure duration");
            village.load(data);
            check(village.isConverting(), "cure survives reload");
            data.putInt("ConversionTime", 1);
            village.load(data);
            village.tick();
            check(village.isRemoved(), "vanilla cure completes");
            var cured =
                    level
                            .getEntitiesOfClass(
                                    net.minecraft.world.entity.npc.Villager.class,
                                    village.getBoundingBox().inflate(1))
                            .stream()
                            .filter(
                                    v ->
                                            v.getVillagerData().getProfession()
                                                    == net.minecraft.world.entity.npc
                                                            .VillagerProfession.LIBRARIAN)
                            .findFirst()
                            .orElseThrow();
            fixtures.add(cured);
            check(
                    cured.getMaxHealth() == 20
                            && Math.abs(cured.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) - .5)
                                    < .0001,
                    "cured vanilla attributes");
            for (var color : DyeColor.values()) {
                var sheep = BlightedMobContent.SHEEP.get().create(level);
                fixtures.add(sheep);
                sheep.setPos(55.5, 150, 80.5);
                sheep.setColor(color);
                level.addFreshEntity(sheep);
                var prior =
                        level
                                .getEntitiesOfClass(
                                        net.minecraft.world.entity.item.ItemEntity.class,
                                        sheep.getBoundingBox().inflate(2))
                                .stream()
                                .map(Entity::getUUID)
                                .collect(java.util.stream.Collectors.toSet());
                sheep.shear(net.minecraft.sounds.SoundSource.PLAYERS);
                var drops =
                        level.getEntitiesOfClass(
                                net.minecraft.world.entity.item.ItemEntity.class,
                                sheep.getBoundingBox().inflate(2),
                                d -> !prior.contains(d.getUUID()));
                check(
                        !drops.isEmpty()
                                && drops.stream().allMatch(d -> d.getItem().is(Items.RED_WOOL)),
                        "red sheared wool " + color);
                check(sheep.getColor() == color && sheep.isSheared(), "shearing preserves coat");
                drops.forEach(Entity::discard);
                sheep.setSheared(false);
                var handDrops =
                        sheep.onSheared(
                                player,
                                new ItemStack(Items.SHEARS),
                                level,
                                sheep.blockPosition(),
                                0);
                check(
                        !handDrops.isEmpty()
                                && handDrops.stream().allMatch(d -> d.is(Items.RED_WOOL)),
                        "Forge hand shearing");
            }
            for (var type : java.util.List.of(BlightedMobContent.STRAY.get())) {
                var archer =
                        (net.minecraft.world.entity.monster.AbstractSkeleton) type.create(level);
                fixtures.add(archer);
                var method =
                        net.minecraft.world.entity.monster.AbstractSkeleton.class.getDeclaredMethod(
                                "getArrow", ItemStack.class, float.class);
                method.setAccessible(true);
                var arrow = (Entity) method.invoke(archer, new ItemStack(Items.ARROW), 1F);
                fixtures.add(arrow);
                var pickup=net.minecraft.world.entity.projectile.AbstractArrow.class.getDeclaredMethod("getPickupItem");
                pickup.setAccessible(true);var stack=(ItemStack)pickup.invoke(arrow);
                var desired=type==BlightedMobContent.STRAY.get()?MobEffects.MOVEMENT_SLOWDOWN:MobEffects.POISON;
                boolean found=false;
                for(var effect: net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(stack))if(effect.getEffect()==desired)found=true;
                check(found,"native tipped arrow effect "+type);
            }
            System.out.println(
                    "BLIGHTED_TRAITS_SERVER_OK: attributes, legacy reload, actual damage, drowned"
                        + " slow, elixir cure and all wool colors");
        } finally {
            fixtures.forEach(Entity::discard);
            player.discard();
        }
    }
}
