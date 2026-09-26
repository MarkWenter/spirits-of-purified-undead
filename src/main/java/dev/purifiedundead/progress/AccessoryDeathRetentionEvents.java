package dev.purifiedundead.progress;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/** Keeps tagged inventory accessories out of death drops and restores them after player cloning. */
public final class AccessoryDeathRetentionEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }

        List<ItemEntity> protectedDrops = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            if (AccessoryDeathRetentionService.isProtected(drop.getItem())) {
                protectedDrops.add(drop);
            }
        }
        if (!protectedDrops.isEmpty()) {
            List<ItemStack> stacks = protectedDrops.stream().map(drop -> drop.getItem().copy()).toList();
            int retained = AccessoryDeathRetentionService.retain(player, stacks);
            for (int index = 0; index < retained; index++) {
                event.getDrops().remove(protectedDrops.get(index));
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement) {
            AccessoryDeathRetentionService.copyAndRestore(original, replacement);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()
                && event.player.tickCount % 20 == 0 && event.player instanceof ServerPlayer player) {
            AccessoryDeathRetentionService.restore(player);
        }
    }
}
