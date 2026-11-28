package dev.purifiedundead.validation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Development-only title-screen compatibility check; no world is opened. */
@Mod.EventBusSubscriber(modid = "purified_undead", value = Dist.CLIENT)
public final class BreathgoldClientSmoke {
    private static boolean checked;

    @SubscribeEvent
    public static void onScreen(ScreenEvent.Init.Post event) {
        if (!Boolean.getBoolean("purified_undead.breathgoldClient")
                || checked
                || !(event.getScreen() instanceof TitleScreen)) return;
        checked = true;
        Minecraft.getInstance()
                .tell(() -> Minecraft.getInstance().setScreen(Boolean.getBoolean("purified_undead.immaculateClient") ? new ImmaculatePreviewScreen() : new BreathgoldPreviewScreen()));
    }
}
