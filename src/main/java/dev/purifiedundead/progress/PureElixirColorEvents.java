package dev.purifiedundead.progress;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
/** Vanilla bottle conversion in older Minecraft drops custom color NBT; restore only our potions. */
@Mod.EventBusSubscriber(modid="purified_undead")
public final class PureElixirColorEvents {
    @SubscribeEvent public static void brewed(PotionBrewEvent.Post event) {
        for(int i=0;i<Math.min(3,event.getLength());i++) PureElixirBrewing.color(event.getItem(i));
    }
}
