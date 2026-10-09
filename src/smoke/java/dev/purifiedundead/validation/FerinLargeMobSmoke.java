package dev.purifiedundead.validation;

import dev.purifiedundead.combat.FerinCombatEvents;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class FerinLargeMobSmoke {
    public static void run(net.minecraft.server.MinecraftServer server) {
        try {
            var level = server.overworld();
            for (boolean hurts : new boolean[] {true, false}) {
                var player =
                        new net.minecraftforge.common.util.FakePlayer(
                                level,
                                new com.mojang.authlib.GameProfile(
                                        java.util.UUID.randomUUID(), "BossSmoke"));
                CuriosApi.getCuriosInventory(player)
                        .orElseThrow(() -> new IllegalStateException("Curios"))
                        .getCurios()
                        .get("ancient_contract")
                        .getStacks()
                        .setStackInSlot(0, new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
                var boss =
                        new Zombie(level) {
                            @Override
                            public boolean hurt(
                                    net.minecraft.world.damagesource.DamageSource source,
                                    float amount) {
                                if (!hurts) return false;
                                setHealth(getHealth() - amount);
                                return true; // Simulate a mod boss omitting living damage hooks.
                            }
                        };
                boss.setPos(0, 64, 3);
                boss.setBoundingBox(new net.minecraft.world.phys.AABB(-8, 64, 2, 8, 80, 18));
                player.setPos(0, 64, 0);
                var events = new FerinCombatEvents();
                events.onMeleeAttempt(
                        new net.minecraftforge.event.entity.player.AttackEntityEvent(player, boss));
                boss.hurt(player.damageSources().playerAttack(player), 2);
                var finish = FerinCombatEvents.class.getDeclaredMethod("finishMeleeObservations");
                finish.setAccessible(true);
                finish.invoke(events);
                boolean started =
                        player.getPersistentData().contains("purified_undead:ferin_state");
                if (started != hurts)
                    throw new IllegalStateException("large boss fallback or immunity guard failed");
                var before = player.getPersistentData().copy();
                finish.invoke(events);
                if (!before.equals(player.getPersistentData()))
                    throw new IllegalStateException("repeated observation");
            }
            var player =
                    new net.minecraftforge.common.util.FakePlayer(
                            level,
                            new com.mojang.authlib.GameProfile(
                                    java.util.UUID.randomUUID(), "MultipartSmoke"));
            CuriosApi.getCuriosInventory(player)
                    .orElseThrow(() -> new IllegalStateException("Curios"))
                    .getCurios()
                    .get("ancient_contract")
                    .getStacks()
                    .setStackInSlot(0, new ItemStack(ModItems.ANCIENT_CONTRACT.get()));
            var dragon =
                    new net.minecraft.world.entity.boss.enderdragon.EnderDragon(
                            net.minecraft.world.entity.EntityType.ENDER_DRAGON, level);
            var part = dragon.getSubEntities()[0];
            var events = new FerinCombatEvents();
            events.onMeleeAttempt(
                    new net.minecraftforge.event.entity.player.AttackEntityEvent(player, part));
            dragon.setHealth(dragon.getHealth() - 2);
            var finish = FerinCombatEvents.class.getDeclaredMethod("finishMeleeObservations");
            finish.setAccessible(true);
            finish.invoke(events);
            if (!player.getPersistentData().contains("purified_undead:ferin_state"))
                throw new IllegalStateException("multipart parent resolution");
            System.out.println(
                    "FERIN_LARGE_MOB_OK: custom hurt without living events, large bounding box, dragon multipart parent, immunity, single-use observation");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
