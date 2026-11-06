package dev.purifiedundead.combat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;

/** External tester entry: real loaded-world collision and lifecycle, not included in release JAR. */
public final class FerinFlightSmoke {
    public static void run(MinecraftServer server) {
        var level = server.overworld();
        var player =
                new net.minecraftforge.common.util.FakePlayer(
                        level,
                        new com.mojang.authlib.GameProfile(
                                java.util.UUID.randomUUID(), "FerinFlightSmoke"));
        player.setPos(40, 80, 40);
        var events = new FerinCombatEvents();
        var mobs = new ArrayList<Zombie>();
        try {
            var launch =
                    FerinCombatEvents.class.getDeclaredMethod(
                            "launchFlight",
                            net.minecraft.server.level.ServerPlayer.class,
                            FerinPlayerState.class,
                            Vec3.class,
                            net.minecraft.world.entity.LivingEntity.class);
            var tick =
                    FerinCombatEvents.class.getDeclaredMethod(
                            "tickFlights", net.minecraft.server.level.ServerPlayer.class);
            var remove =
                    FerinCombatEvents.class.getDeclaredMethod(
                            "removeFerinEntity", net.minecraft.server.level.ServerPlayer.class);
            launch.setAccessible(true);
            tick.setAccessible(true);
            remove.setAccessible(true);
            Zombie target = new Zombie(level);
            target.setNoAi(true);
            target.setPos(40, 80, 53);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(1000);
            target.setHealth(1000);
            level.addFreshEntity(target);
            mobs.add(target);
            var state = new FerinPlayerState();
            long now = level.getGameTime();
            state.combo.onQualifyingHit(now, 5, new FerinComboTimings(40, 10, 40, 16, 4));
            state.captureStage(player.position(), 0, 10, 1);
            launch.invoke(events, player, state, target.getBoundingBox().getCenter(), target);
            // Change the current combo snapshot after launch: the in-flight wave must retain 10.
            state.triggerPreDefenseDamage = 500;
            float before = target.getHealth();
            for (int i = 0; i < 80; i++) tick.invoke(events, player);
            float loss = before - target.getHealth();
            check(loss > 0 && loss <= 7.51, "old flight snapshot and one-hit target set");
            check(
                    level.getEntitiesOfClass(
                                    dev.purifiedundead.entity.FerinEntity.class,
                                    player.getBoundingBox().inflate(32),
                                    e -> e.belongsTo(player.getUUID()))
                            .isEmpty(),
                    "expired visuals removed");
            launch.invoke(events, player, state, target.getBoundingBox().getCenter(), target);
            remove.invoke(events, player);
            before = target.getHealth();
            for (int i = 0; i < 80; i++) tick.invoke(events, player);
            check(before == target.getHealth(), "cancel removes pending damage");
            System.out.println(
                    "FERIN_FLIGHT_OK: independent snapshot, distant real collision, deduplication, finite lifetime, cancellation");
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException("FERIN_FLIGHT_FAILED", error);
        } finally {
            mobs.forEach(Zombie::discard);
            level.getEntitiesOfClass(
                            dev.purifiedundead.entity.FerinEntity.class,
                            player.getBoundingBox().inflate(128),
                            e -> e.belongsTo(player.getUUID()))
                    .forEach(net.minecraft.world.entity.Entity::discard);
        }
    }

    private static void check(boolean condition, String reason) {
        if (!condition) throw new IllegalStateException("FERIN_FLIGHT_FAILED: " + reason);
    }
}
