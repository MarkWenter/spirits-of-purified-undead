package dev.purifiedundead.foundry;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "purified_undead")
public final class FoundrySyncEvents {
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END
                && event.getServer().getTickCount() % 20 == 0)
            FoundryRecipes.retrySync(event.getServer());
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        FoundryRecipes.clearSync();
    }
}
