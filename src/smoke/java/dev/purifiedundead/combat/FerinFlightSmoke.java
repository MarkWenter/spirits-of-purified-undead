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
        // A FakePlayer has no player chunk tickets. Run entirely within the spawn region,
        // high above other smoke fixtures, so level entity queries see each target.
        var spawn = new net.minecraft.world.level.ChunkPos(level.getSharedSpawnPos());
        int x = spawn.getMiddleBlockX(), z = spawn.getMinBlockZ() - 5;
        int y = level.getMaxBuildHeight() - 40;
        player.setPos(x, y, z);
        var events = new FerinCombatEvents();
        var mobs = new ArrayList<Zombie>();
        try {
            // This fixture asserts one noncritical contact. External critical rolls have their own tests.
            var chance = net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("attributeslib", "crit_chance"));
            if (chance != null && player.getAttribute(chance) != null) player.getAttribute(chance).setBaseValue(0);
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
            target.setPos(x, y, z + 13);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(1000);
            target.setHealth(1000);
            check(level.addFreshEntity(target) && level.getEntity(target.getUUID()) == target,
                    "target in loaded spawn region");
            mobs.add(target);
            var state = new FerinPlayerState();
            long now = level.getGameTime();
            state.combo.onQualifyingHit(now, 5, new FerinComboTimings(40, 10, 40, 16, 4));
            state.captureStage(player.position(), 0, 10, 1);
            launch.invoke(events, player, state, target.getBoundingBox().getCenter(), target);
            var visual = level.getEntitiesOfClass(
                    dev.purifiedundead.entity.FerinEntity.class,
                    player.getBoundingBox().inflate(32), e -> e.belongsTo(player.getUUID())).get(0);
            Vec3 release = visual.releasePosition();
            Vec3 releaseDirection = visual.releaseDirection();
            float before = target.getHealth();
            tick.invoke(events, player);
            check(visual.releasePosition().distanceTo(release) < 1e-6
                    && visual.releaseDirection().distanceTo(releaseDirection) < 1e-6,
                    "release pose remains fixed while flight advances");
            check(visual.position().distanceTo(release) > .1, "flight separates from launch");
            // Change the current combo snapshot after launch: the in-flight wave must retain 10.
            state.triggerPreDefenseDamage = 500;
            for (int i = 0; i < 80; i++) tick.invoke(events, player);
            float loss = before - target.getHealth();
            check(loss > 0 && loss <= 7.51, "old flight snapshot and one-hit target set, actual loss " + loss);
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
            // Intercept a new flight before its tracked target. Then move that target off-axis.
            target.setPos(x, y, z + 25);
            var blocker = new Zombie(level);
            blocker.setNoAi(true);
            blocker.setPos(x, y, z + 8);
            check(level.addFreshEntity(blocker) && level.getEntity(blocker.getUUID()) == blocker,
                    "blocker in loaded spawn region");
            mobs.add(blocker);
            launch.invoke(events, player, state, target.getBoundingBox().getCenter(), target);
            var flightMap = FerinCombatEvents.class.getDeclaredField("ferinEntities");
            flightMap.setAccessible(true);
            var active = (java.util.Map<?, ?>) flightMap.get(events);
            var flight = (FerinSlashFlight) ((java.util.List<?>) active.get(player.getUUID())).get(0);
            while (!flight.motion.finished() && !flight.damage.hitTargets.contains(blocker.getUUID()))
                tick.invoke(events, player);
            check(!flight.motion.finished() && !flight.damage.hitTargets.contains(target.getUUID()),
                    "intervening mob hit before tracked target");
            Vec3 straight = flight.motion.direction();
            target.setPos(x + 10, y, z + 25);
            while (!flight.motion.finished()) {
                tick.invoke(events, player);
                check(flight.target == null && flight.motion.direction().distanceTo(straight) < 1e-9,
                        "hit permanently ends steering");
            }
            remove.invoke(events, player);
            System.out.println(
                    "FERIN_FLIGHT_OK: independent snapshot, distant real collision, deduplication, finite lifetime, cancellation, straight after interception");
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
