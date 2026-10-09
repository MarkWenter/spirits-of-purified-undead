package dev.purifiedundead.client;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class FoundryConnectionEvents {
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void disconnect(
            net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        FoundryClientRecipes.clear();
    }
}
