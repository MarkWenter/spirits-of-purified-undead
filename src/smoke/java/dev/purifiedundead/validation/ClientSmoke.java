package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
/** Development-only smoke harness; never included in the distributed JAR. */
@Mod.EventBusSubscriber(modid="purified_undead",value=Dist.CLIENT)
public final class ClientSmoke {
 private static int titleTicks;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
  if(event.phase != TickEvent.Phase.END || !Boolean.getBoolean("purified_undead.smoke"))return;
  if(Boolean.getBoolean("purified_undead.jeiSmoke") || Boolean.getBoolean("purified_undead.diaryClientSmoke") || Boolean.getBoolean("purified_undead.wispSmoke"))return;
  var mc=Minecraft.getInstance();
  if(mc.screen instanceof TitleScreen && mc.getOverlay() == null && ++titleTicks==40) {
   System.out.println("PURIFIED_UNDEAD_CLIENT_SMOKE_OK: title screen and resources ready");
   net.minecraft.client.Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),message->{});
   mc.stop();
  }
 }
}
