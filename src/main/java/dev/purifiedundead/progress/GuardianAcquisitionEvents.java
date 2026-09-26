package dev.purifiedundead.progress;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Awards the Guardians while the player holds the ornament under Slow Falling. */
public final class GuardianAcquisitionEvents {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianAcquisitionEnabled)
                || event.phase != TickEvent.Phase.END || event.player.level().isClientSide()
                || event.player.tickCount % 5 != 0 || !(event.player instanceof ServerPlayer player)
                || !ContractProgressService.hasContract(player) || !player.hasEffect(MobEffects.SLOW_FALLING)) {
            return;
        }
        ItemStack ornament = player.getMainHandItem().is(ModItems.FORMER_ORNAMENT.get())
                ? player.getMainHandItem() : player.getOffhandItem();
        if (!ornament.is(ModItems.FORMER_ORNAMENT.get()) || !WarriorRewardService.grantGuardians(player)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            ornament.shrink(1);
        }
    }
}
