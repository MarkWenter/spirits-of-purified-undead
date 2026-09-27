package dev.purifiedundead.combat;

import dev.purifiedundead.progress.ContractProgressService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Applies the White Witch talisman's final incoming-damage multiplier. */
public final class ContractProtectionEvents {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingDamage(LivingDamageEvent event) {
        float amount = event.getAmount();
        float absorption = 0.0F;
        if (event.getEntity() instanceof ServerPlayer player && ContractProgressService.hasContract(player)
                && !event.getSource().is(ModDamageTypes.ULV_BLIGHT)) {
            amount = amount * ContractProgressService.incomingDamageMultiplier(player);
        }
        var source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer attacker && source.getDirectEntity() == attacker
                && source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)) {
            boolean cursed = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(attacker).map(h ->
                    h.isEquipped(dev.purifiedundead.content.ModItems.ANCIENT_CONTRACT.get())
                    && !h.isEquipped(dev.purifiedundead.content.ModItems.JULIUS_WARRIOR.get())).orElse(false);
            if (cursed) amount = FinalDamageRules.subtractFromHealth(amount, absorption,
                    dev.purifiedundead.config.PurifiedUndeadConfig.get(
                            dev.purifiedundead.config.PurifiedUndeadConfig.VALUES.juliusMeleeDamagePenalty).floatValue());
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            amount += UlvCombatEvents.incomingBlight(player, source, Math.max(0.0F, amount - absorption));
        }
        event.setAmount(amount);
    }
}
