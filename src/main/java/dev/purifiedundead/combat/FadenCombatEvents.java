package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

/** Heretic blight knockback penalty, effective looting, and non-recursive poison rolls. */
public final class FadenCombatEvents {
    private static final UUID KNOCKBACK_PENALTY_ID =
            UUID.fromString("e1f9af72-f2b9-47a8-b63a-87dd085150bb");

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        Equipment equipment = readEquipment(player);
        replaceKnockbackPenalty(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE),
                equipment.unreversed()
                        ? -PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenKnockbackResistancePenalty) : 0.0D);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getAmount() <= 0.0F) {
            return;
        }
        DamageSource source = event.getSource();
        if (event.getEntity() instanceof ServerPlayer target
                && readEquipment(target).unreversed()
                && source.getEntity() instanceof LivingEntity
                && FadenModel.rollPoison(target.getRandom().nextFloat())) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON,
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenPoisonDurationTicks),
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenPoisonAmplifier)));
        }
        if (source.getEntity() instanceof ServerPlayer attacker
                && isDirectPlayerAttack(source, attacker)
                && readEquipment(attacker).reversed()
                && isLegalTarget(attacker, event.getEntity())
                && FadenModel.rollPoison(attacker.getRandom().nextFloat())) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON,
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenPoisonDurationTicks),
                    PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.fadenPoisonAmplifier)), attacker);
        }
    }

    @SubscribeEvent
    public void onLootingLevel(LootingLevelEvent event) {
        DamageSource source = event.getDamageSource();
        if (source != null && source.getEntity() instanceof ServerPlayer player && isReversed(player)) {
            event.setLootingLevel(FadenModel.effectiveEnchantmentLevel(event.getLootingLevel(), true));
        }
    }

    public static boolean isReversed(LivingEntity entity) {
        return readEquipment(entity).reversed();
    }

    private static void replaceKnockbackPenalty(AttributeInstance attribute, double amount) {
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(KNOCKBACK_PENALTY_ID);
        if (existing != null && existing.getAmount() == amount) {
            return;
        }
        if (existing != null) {
            attribute.removeModifier(KNOCKBACK_PENALTY_ID);
        }
        if (amount != 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(KNOCKBACK_PENALTY_ID,
                    "Heretic knockback resistance", amount, AttributeModifier.Operation.ADDITION));
        }
    }

    private static boolean isDirectPlayerAttack(DamageSource source, ServerPlayer player) {
        return source.getEntity() == player && source.getDirectEntity() != null
                && !source.is(ModDamageTypes.FERIN_ASSIST)
                && !source.is(ModDamageTypes.ULV_FOLLOW_UP)
                && !source.is(ModDamageTypes.ULV_BLIGHT)
                && !source.is(ModDamageTypes.ELEINE_MAGIC_ORB);
    }

    private static boolean isLegalTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner || !target.isAlive() || !target.isAttackable() || owner.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player other && !owner.canHarmPlayer(other)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    private static Equipment readEquipment(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).map(handler -> new Equipment(
                handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                handler.isEquipped(ModItems.FADEN_WARRIOR.get()))).orElse(Equipment.NONE);
    }

    private record Equipment(boolean contract, boolean faden) {
        private static final Equipment NONE = new Equipment(false, false);

        private boolean unreversed() {
            return contract && !faden;
        }

        private boolean reversed() {
            return contract && faden;
        }
    }
}
