package dev.purifiedundead.validation;
/** Test-only: prevent file-watcher reload races while smoke cases temporarily set config values. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead")
public final class SmokeConfigIsolation {
    @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void isolate(net.minecraftforge.event.server.ServerAboutToStartEvent e) throws java.io.IOException {
        if(!Boolean.getBoolean("purified_undead.purificationSmoke"))return;
        for(String file:new String[]{"purified_undead-common.toml","purified_undead-slate.toml"})
            com.electronwill.nightconfig.core.file.FileWatcher.defaultInstance().removeWatch(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve(file));
    }
}
