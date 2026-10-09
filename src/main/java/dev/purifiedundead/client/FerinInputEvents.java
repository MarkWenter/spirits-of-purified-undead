package dev.purifiedundead.client;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.network.FerinContinuePacket;
import dev.purifiedundead.network.ModNetwork;
import dev.purifiedundead.content.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = PurifiedUndead.MOD_ID, value = Dist.CLIENT)
public final class FerinInputEvents {
    private static boolean wasDown;
    private static int cooldown;

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.isPaused()) {
            wasDown = false;
            cooldown = 0;
            return;
        }
        boolean down = mc.options.keyAttack.isDown();
        if (cooldown > 0) cooldown--;
        if (down
                && (!wasDown || cooldown == 0)
                && CuriosApi.getCuriosInventory(mc.player)
                        .map(h -> h.isEquipped(ModItems.ANCIENT_CONTRACT.get()))
                        .orElse(false)) {
            ModNetwork.CHANNEL.sendToServer(new FerinContinuePacket());
            cooldown = 4;
        }
        wasDown = down;
    }
}
