package dev.purifiedundead.progress;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
/** Normalize legacy cosmetic data after brewing; item rendering owns the pure-elixir tint. */
@Mod.EventBusSubscriber(modid="purified_undead")
public final class PureElixirColorEvents {
    @SubscribeEvent public static void brewed(PotionBrewEvent.Post event) {
        for(int i=0;i<Math.min(3,event.getLength());i++) PureElixirBrewing.color(event.getItem(i));
    }
}
