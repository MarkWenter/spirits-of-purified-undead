package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModEnchantments;
import dev.purifiedundead.content.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

/** Restores the contract's sealed curses after another mod rewrites its enchantment list. */
public final class ContractCurseIntegrityEvents {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            restoreEquippedContract(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void afterLivingHurt(LivingHurtEvent event) {
        restoreIfPlayer(event.getEntity());
        restoreIfPlayer(event.getSource().getEntity());
    }

    private static void restoreIfPlayer(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            restoreEquippedContract(player);
        }
    }

    private static void restoreEquippedContract(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player)
                .ifPresent(
                        handler ->
                                handler.findCurios(ModItems.ANCIENT_CONTRACT.get())
                                        .forEach(
                                                found ->
                                                        ModEnchantments.applyAllBlightCurses(
                                                                found.stack())));
    }
}
