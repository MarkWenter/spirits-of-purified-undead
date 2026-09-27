package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Applies the Mad Knight's blight, Ulv's stack combo, and non-recursive deferred damage. */
public final class UlvCombatEvents {
    private static final UUID ATTACK_SPEED_ID = UUID.fromString("6988aa32-5c7c-41ca-b839-d6a3cf38f9a8");
    private static final Map<UUID, UlvComboState> COMBOS = new HashMap<>();

    private final Map<CombatHitKey, PendingMelee> pendingHits = new HashMap<>();
    private final List<DeferredDamage> deferredDamage = new ArrayList<>();

    /** Called by the shared direct-melee arithmetic before armor is applied. */
    public static double meleeDamageBonus(ServerPlayer player, LivingEntity target) {
        if (!readEquipment(player).reversed() || !isLegalTarget(player, target)) {
            return 0.0D;
        }
        return COMBOS.computeIfAbsent(player.getUUID(), ignored -> new UlvComboState())
                .damageBonus(player.serverLevel().getGameTime());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void rememberQualifyingMelee(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer player && isDirectPlayerMelee(source, player)
                && readEquipment(player).reversed() && isLegalTarget(player, event.getEntity())
                && Float.isFinite(event.getAmount()) && event.getAmount() > 0.0F) {
            pendingHits.put(new CombatHitKey(event.getEntity().getUUID(), player.getUUID()),
                    new PendingMelee(player.getUUID(),
                    player.serverLevel().getGameTime(), event.getAmount()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHealthDamage(LivingDamageEvent event) {


        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PendingMelee pending = pendingHits.remove(new CombatHitKey(event.getEntity().getUUID(), player.getUUID()));
        if (pending == null || event.getAmount() <= 0.0F
                || !pending.playerId.equals(player.getUUID()) || !isDirectPlayerMelee(source, player)
                || !readEquipment(player).reversed() || !isLegalTarget(player, event.getEntity())) {
            return;
        }
        UlvComboState state = COMBOS.computeIfAbsent(player.getUUID(), ignored -> new UlvComboState());
        if (state.onQualifyingHit(pending.gameTick) == UlvComboState.Result.FOLLOW_UP) {
            float damage = UlvModel.followUpDamage(pending.preDefenseDamage);
            if (damage > 0.0F) {
                deferredDamage.add(DeferredDamage.followUp(player.serverLevel(), event.getEntity().getUUID(),
                        player.getUUID(), pending.gameTick + 1L, damage));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()
                || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Equipment equipment = readEquipment(player);
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            double amount = UlvModel.attackSpeedMultiplier(equipment.contract, equipment.ulv) - 1.0D;
            replaceModifier(attackSpeed, amount);
        }
        if (!equipment.reversed()) {
            COMBOS.remove(player.getUUID());
        } else if (player.tickCount % 10 == 0) {
            UlvComboState state = COMBOS.get(player.getUUID());
            if (state != null && state.expire(player.serverLevel().getGameTime())) {
                COMBOS.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        pendingHits.values().removeIf(hit -> hit.gameTick < now);
        Iterator<DeferredDamage> iterator = deferredDamage.iterator();
        while (iterator.hasNext()) {
            DeferredDamage deferred = iterator.next();
            if (deferred.dueTick > now) {
                continue;
            }
            iterator.remove();
            if (!(deferred.level.getEntity(deferred.targetId) instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }
            if (deferred.level.getPlayerByUUID(deferred.ownerId) instanceof ServerPlayer owner) {
                target.hurt(ModDamageTypes.ulvFollowUp(deferred.level, owner), deferred.amount);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        COMBOS.remove(playerId);
        pendingHits.entrySet().removeIf(entry -> entry.getKey().attackerId().equals(playerId)
                || entry.getKey().targetId().equals(playerId));
        deferredDamage.removeIf(damage -> playerId.equals(damage.ownerId) || playerId.equals(damage.targetId));
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        COMBOS.clear();
        pendingHits.clear();
        deferredDamage.clear();
    }

    public static float incomingBlight(ServerPlayer player, DamageSource source, float healthDamage) {
        if (healthDamage <= 0.0F || source.is(ModDamageTypes.ULV_BLIGHT)
                || source.is(ModDamageTypes.ULV_FOLLOW_UP)) return 0.0F;
        Equipment equipment = readEquipment(player);
        boolean causedByHit = source.getEntity() != null || source.getDirectEntity() != null;
        return equipment.contract && !equipment.ulv && causedByHit
                ? UlvModel.backlashDamage(player.getMaxHealth()) : 0.0F;
    }

    private static Equipment readEquipment(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> new Equipment(
                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                handler.isEquipped(ModItems.ULV_WARRIOR.get()))).orElse(Equipment.NONE);
    }

    private static boolean isDirectPlayerMelee(DamageSource source, Player player) {
        return source.getEntity() == player && source.getDirectEntity() == player
                && source.is(DamageTypes.PLAYER_ATTACK);
    }

    private static boolean isLegalTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || !target.isAttackable() || owner.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player otherPlayer && !owner.canHarmPlayer(otherPlayer)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    private static void replaceModifier(AttributeInstance attribute, double amount) {
        AttributeModifier existing = attribute.getModifier(ATTACK_SPEED_ID);
        if (existing != null && existing.getAmount() == amount
                && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(ATTACK_SPEED_ID);
        }
        if (amount != 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(ATTACK_SPEED_ID,
                    "Mad Knight attack speed", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private record Equipment(boolean contract, boolean ulv) {
        private static final Equipment NONE = new Equipment(false, false);

        boolean reversed() {
            return contract && ulv;
        }
    }

    private record PendingMelee(UUID playerId, long gameTick, float preDefenseDamage) { }


    private record DeferredDamage(ServerLevel level, UUID targetId, UUID ownerId, long dueTick, float amount) {
        static DeferredDamage followUp(ServerLevel level, UUID targetId, UUID ownerId, long dueTick, float amount) {
            return new DeferredDamage(level, targetId, ownerId, dueTick, amount);
        }
    }
}
