package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModEffects;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Applies both directions of the Elder Warrior's blight and its jump-attack stun. */
public final class GrothCombatEvents {
    private static final String STUN_COOLDOWN_KEY = "purified_undead:groth_stun_cooldown_until";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onIncomingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !event.getSource().is(ModDamageTypes.ULV_BLIGHT)) {
            Equipment equipment = readEquipment(player);
            event.setAmount(event.getAmount() * GrothModel.incomingDamageMultiplier(
                    equipment.contract, equipment.groth, player.onGround()));
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Equipment equipment = readEquipment(event.getEntity());
        event.setNewSpeed(event.getNewSpeed() * GrothModel.miningSpeedMultiplier(
                equipment.contract, equipment.groth));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSuccessfulJumpAttack(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player
                || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || event.getAmount() <= 0.0F) {
            return;
        }
        Equipment equipment = readEquipment(player);
        long now = player.serverLevel().getGameTime();
        long cooldownUntil = player.getPersistentData().getLong(STUN_COOLDOWN_KEY);
        boolean jumpAttack = player.fallDistance > 0.0F && !player.onGround();
        if (!GrothModel.canTriggerStun(equipment.reversed(), jumpAttack, now, cooldownUntil)) {
            return;
        }
        if (event.getEntity().addEffect(new MobEffectInstance(ModEffects.STUNNED.get(),
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothStunDurationTicks), 0, false, true, true))) {
            player.getPersistentData().putLong(STUN_COOLDOWN_KEY,
                    now + PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.grothStunCooldownTicks));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void preventStunnedAttack(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker
                && event.getSource().getDirectEntity() == attacker
                && attacker.hasEffect(ModEffects.STUNNED.get())) {
            event.setCanceled(true);
        }
    }

    private static Equipment readEquipment(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> new Equipment(
                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                handler.isEquipped(ModItems.GROTH_WARRIOR.get()))).orElse(Equipment.NONE);
    }

    private record Equipment(boolean contract, boolean groth) {
        private static final Equipment NONE = new Equipment(false, false);

        boolean reversed() {
            return contract && groth;
        }
    }
}
