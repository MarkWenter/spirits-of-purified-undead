package dev.purifiedundead.combat;

import dev.purifiedundead.progress.ContractProgressService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Applies the White Witch talisman's final incoming-damage multiplier. */
public final class ContractProtectionEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && ContractProgressService.hasContract(player)
                && !event.getSource().is(ModDamageTypes.ULV_BLIGHT)) {
            event.setAmount(event.getAmount() * ContractProgressService.incomingDamageMultiplier(player));
        }
    }
}
