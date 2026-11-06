package dev.purifiedundead.combat;

import com.mojang.logging.LogUtils;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.entity.FerinEntity;
import dev.purifiedundead.progress.ContractProgressService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Bridges Forge damage events to the pure Ferin combo and trajectory model. */
public final class FerinCombatEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    // Provisional animation values; these remain configurable design inputs.
    private static final net.minecraft.resources.ResourceLocation CRIT_CHANCE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    "attributeslib", "crit_chance");
    private static final net.minecraft.resources.ResourceLocation CRIT_DAMAGE =
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                    "attributeslib", "crit_damage");

    private final Map<CombatHitKey, PendingHit> pendingHits = new HashMap<>();
    private final Map<UUID, FerinPlayerState> playerStates = new HashMap<>();
    private final Map<UUID, java.util.List<FerinSlashFlight>> ferinEntities = new HashMap<>();
    private final Map<UUID, FerinWispAnchor> wispAnchors = new HashMap<>();

    // One short-lived observation per actual server melee attempt; no client packet can start a
    // combo.
    private final Map<UUID, MeleeObservation> meleeObservations = new HashMap<>();

    private record MeleeObservation(
            ServerPlayer player,
            LivingEntity victim,
            Vec3 aim,
            long tick,
            float health,
            float damage,
            net.minecraftforge.event.entity.player.AttackEntityEvent event) {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMeleeAttempt(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !AbilityPreferences.ferin(player)
                || !FerinEquipment.read(player).contract()) return;
        net.minecraft.world.entity.Entity attacked = event.getTarget();
        net.minecraft.world.entity.Entity parent =
                attacked instanceof net.minecraftforge.entity.PartEntity<?> part
                        ? part.getParent()
                        : attacked;
        if (!(parent instanceof LivingEntity victim) || !isLegalTarget(player, victim)) return;
        float strength = player.getAttackStrengthScale(0.5F);
        float damage =
                (float)
                                player.getAttributeValue(
                                        net.minecraft.world.entity.ai.attributes.Attributes
                                                .ATTACK_DAMAGE)
                        * (0.2F + strength * strength * 0.8F);
        Vec3 eye = player.getEyePosition();
        var box = attacked.getBoundingBox();
        // Aim at the contacted body/part, not the remote centre of a giant boss.
        Vec3 aim =
                box.clip(
                                eye,
                                eye.add(
                                        player.getLookAngle()
                                                .scale(
                                                        Math.min(
                                                                FerinFlightMotion.MAX_DISTANCE,
                                                                eye.distanceTo(box.getCenter())
                                                                        + 8))))
                        .orElseGet(
                                () ->
                                        new Vec3(
                                                net.minecraft.util.Mth.clamp(
                                                        eye.x, box.minX, box.maxX),
                                                net.minecraft.util.Mth.clamp(
                                                        eye.y, box.minY, box.maxY),
                                                net.minecraft.util.Mth.clamp(
                                                        eye.z, box.minZ, box.maxZ)));
        meleeObservations.put(
                player.getUUID(),
                new MeleeObservation(
                        player,
                        victim,
                        aim,
                        player.serverLevel().getGameTime(),
                        victim.getHealth(),
                        damage,
                        event));
    }

    private void finishMeleeObservations() {
        for (MeleeObservation hit : meleeObservations.values()) {
            // Bosses may implement hurt themselves and omit LivingHurt/LivingDamage.
            // Require an observed health loss; blocked/immune/missed attacks cannot summon Ferin.
            if (!hit.event.isCanceled()
                    && hit.player.level() == hit.victim.level()
                    && hit.player.isAlive()
                    && hit.victim.getHealth() < hit.health
                    && FerinEquipment.read(hit.player).contract()) {
                var pending =
                        pendingHits.get(
                                new CombatHitKey(hit.victim.getUUID(), hit.player.getUUID()));
                float damage =
                        pending != null && pending.gameTick == hit.tick
                                ? pending.preDefenseDamage
                                : hit.damage;
                if (Float.isFinite(damage) && damage > 0)
                    triggerCombo(
                            hit.player,
                            hit.aim,
                            hit.victim,
                            new PendingHit(hit.player.getUUID(), hit.tick, damage));
            }
        }
        meleeObservations.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onFerinBlightMeleeDamage(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player
                && isDirectPlayerMelee(source, player)
                && Float.isFinite(event.getAmount())
                && event.getAmount() >= 0.0F) {
            FerinEquipment.State equipment = FerinEquipment.read(player);
            event.setAmount(
                    FerinBlightModel.meleeDamage(
                            event.getAmount(),
                            equipment.contract(),
                            equipment.ferinWarrior(),
                            UlvCombatEvents.meleeDamageBonus(player, event.getEntity())));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player
                && isDirectPlayerMelee(source, player)
                && Float.isFinite(event.getAmount())
                && event.getAmount() > 0.0F) {
            pendingHits.put(
                    new CombatHitKey(event.getEntity().getUUID(), player.getUUID()),
                    new PendingHit(
                            player.getUUID(),
                            player.serverLevel().getGameTime(),
                            event.getAmount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        applyFerinLifesteal(event);
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingHit pending =
                pendingHits.remove(new CombatHitKey(event.getEntity().getUUID(), player.getUUID()));
        if (pending == null
                || !pending.playerId.equals(player.getUUID())
                || !isDirectPlayerMelee(source, player)) {
            return;
        }

        boolean equipped =
                CuriosApi.getCuriosInventory(player)
                        .map(handler -> handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()))
                        .orElse(false);
        if (!FerinMeleeTrigger.qualifies(
                !player.level().isClientSide(),
                equipped,
                true,
                source.is(DamageTypes.PLAYER_ATTACK),
                event.getAmount())) {
            return;
        }

        MeleeObservation observed = meleeObservations.remove(player.getUUID());
        Vec3 aim =
                observed != null && observed.victim == event.getEntity()
                        ? observed.aim
                        : hitPoint(player, event.getEntity());
        triggerCombo(player, aim, event.getEntity(), pending);
    }

    private void triggerCombo(
            ServerPlayer player, Vec3 aim, LivingEntity victim, PendingHit pending) {
        if (!AbilityPreferences.ferin(player)) return;
        FerinPlayerState runtime = stateFor(player);
        if (!runtime.attackTickGate.accept(pending.gameTick)) {
            FerinPlayerStateStorage.save(player, runtime);
            return;
        }

        FerinComboState.Result result =
                runtime.combo.onQualifyingHit(
                        pending.gameTick,
                        ContractProgressService.ferinMaxStages(player),
                        timings());
        if (result == FerinComboState.Result.STARTED || result == FerinComboState.Result.ADVANCED) {
            float criticalMultiplier = rollStageCritical(player).multiplier();
            runtime.captureStageTowards(
                    player.position(),
                    aim,
                    player.getYRot(),
                    pending.preDefenseDamage,
                    criticalMultiplier);
            runtime.lastTargetId = victim.getUUID();
            launchFlight(player, runtime, aim, victim);
            player.displayClientMessage(
                    Component.translatable(
                            "message.purified_undead.ferin.stage",
                            runtime.combo.stage(),
                            String.format(Locale.ROOT, "%.2f", pending.preDefenseDamage),
                            String.format(Locale.ROOT, "%.2f", runtime.stageCriticalMultiplier)),
                    true);
            LOGGER.debug(
                    "Ferin stage {} started for {} from pre-defense damage {}",
                    runtime.combo.stage(),
                    player.getGameProfile().getName(),
                    pending.preDefenseDamage);
        }
        if (result == FerinComboState.Result.LOCKED) {
            runtime.continuation.request(runtime.combo.stage(), runtime.combo.stageStartedAt());
            runtime.continuationDamage = pending.preDefenseDamage;
            runtime.continuationTarget = aim;
            runtime.continuationTargetId = victim.getUUID();
        }
        FerinPlayerStateStorage.save(player, runtime);
    }

    public static final class ContinueInput extends net.minecraftforge.eventbus.api.Event {
        final ServerPlayer player;

        public ContinueInput(ServerPlayer player) {
            this.player = player;
        }
    }

    @SubscribeEvent
    public void onContinueInput(ContinueInput event) {
        ServerPlayer player = event.player;
        FerinPlayerState runtime = playerStates.get(player.getUUID());
        if (runtime == null
                || !player.isAlive()
                || player.isSpectator()
                || runtime.combo.stage() == 0
                || runtime.combo.exiting()
                || runtime.combo.stage() >= ContractProgressService.ferinMaxStages(player)) return;
        long now = player.serverLevel().getGameTime();
        if (now - runtime.combo.stageStartedAt() >= timings().comboWindow()
                || runtime.lastContinuationTick == now
                || runtime.attackTickGate.lastAcceptedTick() == now
                || !AbilityPreferences.ferin(player)
                || !FerinEquipment.read(player).contract()) return;
        runtime.lastContinuationTick = now;
        if (runtime.continuation.pending(runtime.combo.stage(), runtime.combo.stageStartedAt()))
            return;
        runtime.continuation.request(runtime.combo.stage(), runtime.combo.stageStartedAt());
        runtime.continuationDamage = runtime.triggerPreDefenseDamage;
        runtime.continuationTarget = null;
        runtime.continuationTargetId = null;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long gameTick = event.getServer().overworld().getGameTime();
        finishMeleeObservations();
        pendingHits.values().removeIf(pending -> pending.gameTick < gameTick);
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isAlive() && !player.isSpectator() && FerinEquipment.read(player).contract())
                wispAnchors
                        .computeIfAbsent(player.getUUID(), ignored -> new FerinWispAnchor())
                        .update(player);
            else wispAnchors.remove(player.getUUID());
            FerinPlayerState runtime = playerStates.get(player.getUUID());
            if (runtime == null && FerinPlayerStateStorage.has(player)) {
                runtime = stateFor(player);
            }
            if (runtime == null) {
                continue;
            }
            // An idle combo has no ticking state. Do not scan chunks or rebuild NBT forever
            // merely because this player summoned Ferin at some earlier point in the world.
            if (runtime.combo.stage() == 0
                    && !runtime.combo.exiting()
                    && !ferinEntities.containsKey(player.getUUID())) continue;
            if (!player.isAlive()
                    || player.isSpectator()
                    || !AbilityPreferences.ferin(player)
                    || !FerinEquipment.read(player).contract()) {
                runtime.cancelActive();
                removeFerinEntity(player);
                FerinPlayerStateStorage.save(player, runtime);
                continue;
            }
            final FerinPlayerState activeState = runtime;
            activeState.combo.tick(
                    gameTick, ContractProgressService.ferinMaxStages(player), timings());
            boolean canContinue =
                    player.isAlive()
                            && !player.isSpectator()
                            && FerinEquipment.read(player).contract()
                            && !activeState.combo.exiting()
                            && activeState.combo.stage() > 0
                            && activeState.combo.stage()
                                    < ContractProgressService.ferinMaxStages(player);
            if (activeState.continuation.consume(
                    activeState.combo.stage(),
                    activeState.combo.stageStartedAt(),
                    gameTick,
                    timings().comboLock(),
                    canContinue)) {
                if (activeState.combo.onQualifyingHit(
                                gameTick, ContractProgressService.ferinMaxStages(player), timings())
                        == FerinComboState.Result.ADVANCED) {
                    float damage = activeState.continuationDamage;
                    Vec3 target = activeState.continuationTarget;
                    UUID targetId =
                            target == null
                                    ? activeState.lastTargetId
                                    : activeState.continuationTargetId;
                    LivingEntity victim =
                            targetId != null
                                            && player.serverLevel().getEntity(targetId)
                                                    instanceof LivingEntity living
                                            && isLegalTarget(player, living)
                                    ? living
                                    : null;
                    if (target == null && victim != null) target = hitPoint(player, victim);
                    if (victim == null) target = null;
                    float critical = rollStageCritical(player).multiplier();
                    if (target == null)
                        activeState.captureStage(
                                player.position(), player.getYRot(), damage, critical);
                    else
                        activeState.captureStageTowards(
                                player.position(), target, player.getYRot(), damage, critical);
                    activeState.lastTargetId = victim == null ? null : victim.getUUID();
                    launchFlight(player, activeState, target, victim);
                    player.displayClientMessage(
                            Component.translatable(
                                    "message.purified_undead.ferin.stage",
                                    activeState.combo.stage(),
                                    String.format(Locale.ROOT, "%.2f", damage),
                                    String.format(Locale.ROOT, "%.2f", critical)),
                            true);
                }
            }
            tickFlights(player);
            FerinPlayerStateStorage.save(player, activeState);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        FerinPlayerState runtime = playerStates.get(event.getOriginal().getUUID());
        if (runtime == null && FerinPlayerStateStorage.has(event.getOriginal())) {
            runtime = FerinPlayerStateStorage.load(event.getOriginal());
        }
        if (runtime != null) {
            runtime.cancelActive();
            FerinPlayerStateStorage.save(event.getOriginal(), runtime);
        }
        if (event.getOriginal() instanceof ServerPlayer original) removeFerinEntity(original);
        FerinPlayerStateStorage.copy(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        FerinPlayerState runtime = stateFor(player);
        runtime.cancelActive();
        removeFerinEntity(player);
        meleeObservations.remove(player.getUUID());
        pendingHits
                .entrySet()
                .removeIf(entry -> entry.getKey().attackerId().equals(player.getUUID()));
        FerinPlayerStateStorage.save(player, runtime);
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            removeFerinEntity(player);
        }
        UUID playerId = event.getEntity().getUUID();
        meleeObservations.remove(playerId);
        pendingHits
                .entrySet()
                .removeIf(
                        entry ->
                                entry.getKey().attackerId().equals(playerId)
                                        || entry.getKey().targetId().equals(playerId));
        FerinPlayerState runtime = playerStates.remove(event.getEntity().getUUID());
        if (runtime != null) {
            runtime.cancelActive();
            FerinPlayerStateStorage.save(event.getEntity(), runtime);
        }
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        meleeObservations.clear();
        pendingHits.clear();
        playerStates.clear();
        ferinEntities.clear();
        wispAnchors.clear();
    }

    private static boolean isDirectPlayerMelee(DamageSource source, Player player) {
        return source.getEntity() == player
                && source.getDirectEntity() == player
                && source.is(DamageTypes.PLAYER_ATTACK);
    }

    private static void applyFerinLifesteal(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)
                || !FerinEquipment.read(player).reversed()) {
            return;
        }
        boolean eligible =
                isDirectPlayerMelee(source, player) || source.is(ModDamageTypes.FERIN_ASSIST);
        if (!eligible) {
            return;
        }
        // Some damage trackers dispatch this event before consuming absorption. In vanilla,
        // a positive health-damage event already has no absorption left, so this is equivalent.
        float healthDamage =
                Math.max(0, event.getAmount() - event.getEntity().getAbsorptionAmount());
        float healing = FerinBlightModel.lifesteal(healthDamage, event.getEntity().getHealth());
        if (healing > 0.0F) {
            player.heal(healing);
        }
    }

    private FerinPlayerState stateFor(ServerPlayer player) {
        return playerStates.computeIfAbsent(
                player.getUUID(), ignored -> FerinPlayerStateStorage.load(player));
    }

    private static FerinComboTimings timings() {
        int lock =
                Math.max(
                        9,
                        PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinComboLockTicks));
        int window =
                Math.max(
                        Math.max(40, lock + 20),
                        PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.ferinComboWindowTicks));
        return new FerinComboTimings(
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinSummonCooldownTicks),
                lock,
                window,
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinFinalActionDurationTicks),
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinExitDurationTicks));
    }

    private static Vec3 hitPoint(ServerPlayer player, LivingEntity target) {
        Vec3 eye = player.getEyePosition();
        var box = target.getBoundingBox();
        double ray = Math.min(FerinFlightMotion.MAX_DISTANCE, eye.distanceTo(box.getCenter()) + 8);
        return box.clip(eye, eye.add(player.getLookAngle().scale(ray)))
                .orElseGet(
                        () ->
                                new Vec3(
                                        net.minecraft.util.Mth.clamp(eye.x, box.minX, box.maxX),
                                        net.minecraft.util.Mth.clamp(eye.y, box.minY, box.maxY),
                                        net.minecraft.util.Mth.clamp(eye.z, box.minZ, box.maxZ)));
    }

    private void launchFlight(
            ServerPlayer owner, FerinPlayerState runtime, Vec3 aim, LivingEntity victim) {
        Vec3 origin =
                wispAnchors
                        .computeIfAbsent(owner.getUUID(), ignored -> new FerinWispAnchor())
                        .update(owner);
        if (aim == null)
            aim =
                    origin.add(
                            owner.getLookAngle()
                                    .scale(FerinBladeTrajectory.reach(runtime.combo.stage())));
        if (!FerinFlightMotion.finite(aim)
                || origin.distanceTo(aim) > FerinFlightMotion.MAX_DISTANCE) return;
        var flights =
                ferinEntities.computeIfAbsent(
                        owner.getUUID(), ignored -> new java.util.ArrayList<>());
        // At most one release per tick and 76 flight ticks, even with custom cooldowns.
        if (flights.size() >= 80) {
            flights.remove(0).discard();
        }
        var flight =
                new FerinSlashFlight(
                        runtime,
                        origin,
                        aim,
                        owner.getLookAngle(),
                        victim,
                        PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.ferinFlightMinimumSpeed),
                        PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.ferinFlightArrivalTicks),
                        PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.ferinFlightTurnDegrees));
        flights.add(flight);
        var visual = ModEntities.FERIN.get().create(owner.serverLevel());
        if (visual != null) {
            visual.setOwnerUuid(owner.getUUID());
            visual.syncStage(flight.stage, flight.startedAt);
            visual.syncFlight(
                    origin,
                    flight.motion.direction(),
                    flight.motion.lifetime(),
                    0,
                    flight.motion.speed());
            if (owner.serverLevel().addFreshEntity(visual)) flight.visual = visual;
        }
    }

    private void tickFlights(ServerPlayer owner) {
        var flights = ferinEntities.get(owner.getUUID());
        if (flights == null) return;
        var iterator = flights.iterator();
        while (iterator.hasNext()) {
            var flight = iterator.next();
            if (flight.motion.finished()) {
                flight.discard();
                iterator.remove();
                continue;
            }
            Vec3 previous = flight.motion.position(), previousDirection = flight.motion.direction();
            Vec3 target = flight.targetPosition(owner.serverLevel());
            flight.motion.step(target);
            processFlight(owner, flight, previous, previousDirection);
            if (flight.visual != null && !flight.visual.isRemoved()) {
                flight.visual.refreshOwnerLease();
                flight.visual.syncFlight(
                        flight.motion.position(),
                        flight.motion.direction(),
                        flight.motion.lifetime(),
                        flight.motion.age(),
                        flight.motion.speed());
            }
        }
        if (flights.isEmpty()) ferinEntities.remove(owner.getUUID());
    }

    private void removeFerinEntity(ServerPlayer owner) {
        var flights = ferinEntities.remove(owner.getUUID());
        if (flights != null) flights.forEach(FerinSlashFlight::discard);
        wispAnchors.remove(owner.getUUID());
    }

    private static FerinSweptBlade.WorldPose flightPose(
            int stage, double progress, Vec3 origin, FerinFlightMotion.Basis basis) {
        var pose = FerinBladeTrajectory.blade(stage, progress, 0);
        var a = pose.root();
        var b = pose.tip();
        return new FerinSweptBlade.WorldPose(
                basis.point(origin, a.right(), a.up(), a.forward()),
                basis.point(origin, b.right(), b.up(), b.forward()),
                pose.thickness() + .20);
    }

    private static void processFlight(
            ServerPlayer owner, FerinSlashFlight flight, Vec3 previous, Vec3 previousDirection) {
        var sweeps = new java.util.ArrayList<WorldSweep>(181);
        double progress =
                FerinBladeTrajectory.progress(
                        flight.stage,
                        1
                                + flight.motion.age()
                                        * (FerinBladeTrajectory.sweepEnd(flight.stage) - 1.0)
                                        / Math.min(
                                                flight.motion.lifetime(),
                                                FerinBladeTrajectory.sweepEnd(flight.stage) - 1));
        var previousBasis = FerinFlightMotion.basis(previousDirection);
        var currentBasis = FerinFlightMotion.basis(flight.motion.direction());
        // Every radial slice of the fan/ring sweeps continuously across the entire tick.
        for (int i = 0; i <= 180; i++) {
            double p = progress * i / 180.0;
            sweeps.add(
                    new WorldSweep(
                            flightPose(flight.stage, p, previous, previousBasis),
                            flightPose(flight.stage, p, flight.motion.position(), currentBasis)));
        }
        // Narrow broad-phase segments avoid the enormous cube of a long diagonal shot.
        int sections =
                Math.max(1, (int) Math.ceil(previous.distanceTo(flight.motion.position()) / 16));
        var checked = new java.util.HashSet<UUID>();
        for (int i = 0; i < sections; i++) {
            Vec3 a = previous.lerp(flight.motion.position(), i / (double) sections);
            Vec3 b = previous.lerp(flight.motion.position(), (i + 1.0) / sections);
            var bounds =
                    new net.minecraft.world.phys.AABB(a, b)
                            .inflate(FerinBladeTrajectory.reach(flight.stage) + .55);
            applyBladeHits(owner.serverLevel(), owner, flight.damage, sweeps, bounds, checked);
        }
    }

    private record WorldSweep(
            FerinSweptBlade.WorldPose from,
            FerinSweptBlade.WorldPose to,
            net.minecraft.world.phys.AABB bounds) {
        WorldSweep(FerinSweptBlade.WorldPose from, FerinSweptBlade.WorldPose to) {
            this(from, to, FerinSweptBlade.bounds(from, to));
        }
    }

    private static void applyBladeHits(
            ServerLevel level,
            ServerPlayer owner,
            FerinPlayerState runtime,
            java.util.List<WorldSweep> sweeps,
            net.minecraft.world.phys.AABB bounds,
            java.util.Set<UUID> checked) {
        for (LivingEntity target :
                level.getEntitiesOfClass(
                        LivingEntity.class, bounds, candidate -> isLegalTarget(owner, candidate))) {
            if (!checked.add(target.getUUID())
                    || runtime.hitTargets.contains(target.getUUID())
                    || sweeps.stream()
                            .noneMatch(
                                    sweep ->
                                            sweep.bounds().intersects(target.getBoundingBox())
                                                    && FerinSweptBlade.intersects(
                                                            sweep.from(),
                                                            sweep.to(),
                                                            target.getBoundingBox()))) {
                continue;
            }
            runtime.hitTargets.add(target.getUUID());
            float amount =
                    FerinDamageModel.baseDamage(
                            runtime.combo.stage(), runtime.triggerPreDefenseDamage);
            amount *= runtime.stageCriticalMultiplier;
            if (dev.purifiedundead.slate.MemoryEffects.active(owner, "ferin"))
                amount *=
                        1
                                + dev.purifiedundead.slate.SlateConfig.get(
                                        dev.purifiedundead.slate.SlateConfig.ferinBonus);
            if (!Float.isFinite(amount) || amount < 0) continue;
            boolean hoenirMarked = HoenirCombatEvents.hasMark(owner, target);
            amount = HoenirModel.ferinDamage(amount, hoenirMarked);
            if (!Float.isFinite(amount) || amount < 0) continue;
            int previousInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged;
            try {
                damaged = target.hurt(ModDamageTypes.ferinAssist(level, owner), amount);
            } finally {
                target.invulnerableTime =
                        Math.max(previousInvulnerableTime, target.invulnerableTime);
            }
            if (damaged && hoenirMarked) {
                HoenirCombatEvents.consumeMark(owner, target);
            }
        }
    }

    private static boolean isLegalTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner
                || !target.isAlive()
                || !target.isAttackable()
                || owner.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player otherPlayer && !owner.canHarmPlayer(otherPlayer)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable
                && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    private record PendingHit(UUID playerId, long gameTick, float preDefenseDamage) {}

    public static FerinCriticalModel.Result rollStageCritical(ServerPlayer player) {
        Attribute chance = ForgeRegistries.ATTRIBUTES.getValue(CRIT_CHANCE);
        Attribute damage = ForgeRegistries.ATTRIBUTES.getValue(CRIT_DAMAGE);
        if (chance == null
                || damage == null
                || player.getAttribute(chance) == null
                || player.getAttribute(damage) == null) {
            return new FerinCriticalModel.Result(1.0F, false);
        }
        return FerinCriticalModel.rollExternal(
                player.getAttributeValue(chance),
                player.getAttributeValue(damage),
                () -> player.getRandom().nextFloat());
    }
}
