package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModDamageTypeTags;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.entity.EleineMagicOrbEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Dark Witch blight, water behavior, and server-owned magic-orb spawning. */
public final class EleineCombatEvents {
    private static final UUID SWIM_SPEED_ID =
            UUID.fromString("7592cb17-937b-4acd-9ac1-d57d17583f28");

    private final Map<CombatHitKey, PendingMelee> pendingHits = new HashMap<>();
    private final Map<UUID, FerinMeleeTrigger.AttackTickGate> orbAttackGates = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMagicHurt(LivingHurtEvent event) {
        if (!event.getSource().is(ModDamageTypeTags.MAGIC)
                || !Float.isFinite(event.getAmount())
                || event.getAmount() < 0.0F) {
            return;
        }
        float amount = event.getAmount();
        if (event.getEntity() instanceof ServerPlayer target) {
            Equipment targetEquipment = readEquipment(target);
            amount =
                    EleineModel.incomingMagicDamage(
                            amount, targetEquipment.contract, targetEquipment.eleine);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            Equipment attackerEquipment = readEquipment(attacker);
            amount =
                    EleineModel.outgoingMagicDamage(
                            amount, attackerEquipment.contract, attackerEquipment.eleine);
        }
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void rememberMelee(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player
                && isDirectPlayerMelee(source, player)
                && readEquipment(player).reversed()
                && isLegalTarget(player, event.getEntity())
                && Float.isFinite(event.getAmount())
                && event.getAmount() > 0.0F) {
            pendingHits.put(
                    new CombatHitKey(event.getEntity().getUUID(), player.getUUID()),
                    new PendingMelee(
                            player.getUUID(),
                            player.serverLevel().getGameTime(),
                            event.getAmount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMeleeDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingMelee pending =
                pendingHits.remove(new CombatHitKey(event.getEntity().getUUID(), player.getUUID()));
        if (pending == null
                || event.getAmount() <= 0.0F
                || !pending.playerId.equals(player.getUUID())
                || !isDirectPlayerMelee(source, player)
                || !readEquipment(player).reversed()
                || !isLegalTarget(player, event.getEntity())) {
            return;
        }
        FerinMeleeTrigger.AttackTickGate gate =
                orbAttackGates.computeIfAbsent(
                        player.getUUID(), ignored -> new FerinMeleeTrigger.AttackTickGate());
        if (!gate.accept(pending.gameTick)
                || !(dev.purifiedundead.slate.MemoryEffects.active(player, "eleine")
                        ? player.getRandom().nextDouble()
                                < dev.purifiedundead.slate.SlateConfig.get(
                                        dev.purifiedundead.slate.SlateConfig.orbChance)
                        : EleineModel.rollOrb(player.getRandom().nextFloat()))) {
            return;
        }
        float damage =
                dev.purifiedundead.slate.MemoryEffects.active(player, "eleine")
                        ? (float)
                                (pending.preDefenseDamage
                                        * dev.purifiedundead.slate.SlateConfig.get(
                                                dev.purifiedundead.slate.SlateConfig.orbDamage))
                        : EleineModel.orbDamage(pending.preDefenseDamage);
        EleineMagicOrbEntity orb = ModEntities.ELEINE_MAGIC_ORB.get().create(player.serverLevel());
        if (orb != null && damage > 0.0F) {
            orb.configure(player, event.getEntity(), damage);
            player.serverLevel().addFreshEntity(orb);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Equipment equipment = readEquipment(player);
        updateSwimSpeed(player, equipment);
        if (equipment.reversed() && player.isInWater()) {
            if (player.tickCount % 20 == 0) {
                player.addEffect(
                        new MobEffectInstance(
                                MobEffects.WATER_BREATHING,
                                PurifiedUndeadConfig.get(
                                        PurifiedUndeadConfig.VALUES.eleineWaterBreathingTicks),
                                0,
                                true,
                                false,
                                true));
            }
        } else if (equipment.unreversed()
                && player.isEyeInFluid(FluidTags.WATER)
                && !player.getAbilities().invulnerable
                && !player.hasEffect(MobEffects.WATER_BREATHING)
                && !player.hasEffect(MobEffects.CONDUIT_POWER)) {
            player.setAirSupply(Math.max(-20, player.getAirSupply() - 1));
        }
        if (!equipment.reversed()) {
            orbAttackGates.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long now = event.getServer().overworld().getGameTime();
            pendingHits.values().removeIf(hit -> hit.gameTick < now);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        pendingHits
                .entrySet()
                .removeIf(
                        entry ->
                                entry.getKey().attackerId().equals(playerId)
                                        || entry.getKey().targetId().equals(playerId));
        orbAttackGates.remove(playerId);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        pendingHits.clear();
        orbAttackGates.clear();
    }

    private static void updateSwimSpeed(ServerPlayer player, Equipment equipment) {
        AttributeInstance swimSpeed = player.getAttribute(ForgeMod.SWIM_SPEED.get());
        if (swimSpeed == null) {
            return;
        }
        double amount =
                player.isInWater()
                        ? EleineModel.swimSpeedMultiplier(equipment.contract, equipment.eleine)
                                - 1.0D
                        : 0.0D;
        AttributeModifier existing = swimSpeed.getModifier(SWIM_SPEED_ID);
        if (existing != null
                && existing.getAmount() == amount
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return;
        }
        if (existing != null) {
            swimSpeed.removeModifier(SWIM_SPEED_ID);
        }
        if (amount != 0.0D) {
            swimSpeed.addTransientModifier(
                    new AttributeModifier(
                            SWIM_SPEED_ID,
                            "Dark Witch swim speed",
                            amount,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private static Equipment readEquipment(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(
                        handler ->
                                new Equipment(
                                        handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                                        handler.isEquipped(ModItems.ELEINE_WARRIOR.get())))
                .orElse(Equipment.NONE);
    }

    private static boolean isDirectPlayerMelee(DamageSource source, Player player) {
        return source.getEntity() == player
                && source.getDirectEntity() == player
                && source.is(DamageTypes.PLAYER_ATTACK);
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

    private record Equipment(boolean contract, boolean eleine) {
        private static final Equipment NONE = new Equipment(false, false);

        boolean reversed() {
            return contract && eleine;
        }

        boolean unreversed() {
            return contract && !eleine;
        }
    }

    private record PendingMelee(UUID playerId, long gameTick, float preDefenseDamage) {}
}
