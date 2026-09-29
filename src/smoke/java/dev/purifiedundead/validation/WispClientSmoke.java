package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.purifiedundead.client.*;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
@Mod.EventBusSubscriber(modid="purified_undead",value=Dist.CLIENT)
public final class WispClientSmoke {
 private static int title,ticks; private static boolean loading;
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
  if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
  if(!Boolean.getBoolean("purified_undead.wispSmoke"))return;
  var mc=Minecraft.getInstance();
  try {
   if(!loading && mc.screen instanceof TitleScreen && mc.getOverlay()==null && ++title>40){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"wisp-validation");return;}
   if(mc.level==null || mc.player==null || mc.getOverlay()!=null)return;
   if(mc.screen!=null)mc.setScreen(null);
   ticks++;
   if(ticks==60) {
    mc.options.pauseOnLostFocus=false;
    mc.getSingleplayerServer().execute(()->{
     var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
     var level=p.serverLevel();level.setDayTime(18000);
     p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
     for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=79;y<=85;y++)
      level.setBlock(new net.minecraft.core.BlockPos(x,y,z),(y==79||y==85||Math.abs(x)==7||Math.abs(z)==7?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(),3);
     p.teleportTo(level,0,80,0,0,10);
     CuriosApi.getCuriosInventory(p).ifPresent(h->h.getCurios().get("ancient_contract").getStacks().setStackInSlot(0,new ItemStack(ModItems.ANCIENT_CONTRACT.get())));
    });
   }
   if(ticks==160)mc.getSingleplayerServer().execute(()->{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    CuriosApi.getCuriosInventory(p).ifPresent(h->{var a=h.getCurios().get("undead_warrior").getStacks();
     net.minecraft.world.item.Item[] items={ModItems.FERIN_WARRIOR.get(),ModItems.GROTH_WARRIOR.get(),ModItems.JULIUS_WARRIOR.get(),ModItems.GUARDIAN_WARRIORS.get(),ModItems.ULV_WARRIOR.get(),ModItems.ELEINE_WARRIOR.get(),ModItems.HOENIR_WARRIOR.get(),ModItems.FADEN_WARRIOR.get()};
     for(int i=0;i<8;i++)a.setStackInSlot(i,new ItemStack(items[i]));
    });
   });
   if(ticks==280) {
    var w=ContractWispClient.findFor(mc.player);
    if(w==null || w.visualLight()!=15 || w.tickCount<50)throw new IllegalStateException("wisp absent, frozen or equipment sync failed: "+w);
    double level=dynamic(w.blockPosition());
    if(!WispDynamicLights.backend().equals("none") && level<10)throw new IllegalStateException("dynamic light missing: "+level);
    if(mc.level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,w.blockPosition())!=0)throw new IllegalStateException("world light changed");
    System.out.println("WISP_LIGHT_OK backend="+WispDynamicLights.backend()+" luminance="+w.visualLight()+" terrain="+level+" age="+w.tickCount);
    net.minecraft.client.Screenshot.grab(mc.gameDirectory,"wisp-full.png",mc.getMainRenderTarget(),m->{});
   }
   if(ticks==300)mc.getSingleplayerServer().execute(()->{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    CuriosApi.getCuriosInventory(p).ifPresent(h->{var a=h.getCurios().get("undead_warrior").getStacks();for(int i=0;i<a.getSlots();i++)a.setStackInSlot(i,ItemStack.EMPTY);});
   });
   if(ticks==360) {
    var w=ContractWispClient.findFor(mc.player);if(w==null || w.visualLight()!=3)throw new IllegalStateException("dim light sync failed");
    System.out.println("WISP_DIM_OK luminance="+w.visualLight()+" terrain="+dynamic(w.blockPosition()));
    net.minecraft.client.Screenshot.grab(mc.gameDirectory,"wisp-dim.png",mc.getMainRenderTarget(),m->{});
   }
   if(ticks==380)mc.getSingleplayerServer().execute(()->{
    var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
    CuriosApi.getCuriosInventory(p).ifPresent(h->h.getCurios().get("ancient_contract").getStacks().setStackInSlot(0,ItemStack.EMPTY));
   });
   if(ticks==440) {
    if(ContractWispClient.activeCount()!=0 || dynamic(mc.player.blockPosition())>0)throw new IllegalStateException("orphan light");
    net.minecraft.client.Screenshot.grab(mc.gameDirectory,"wisp-removed.png",mc.getMainRenderTarget(),m->{});
    System.out.println("PURIFIED_UNDEAD_WISP_CLIENT_OK: full/dim/sync/no-world-light/cleanup");mc.stop();
   }
  }catch(Throwable e){e.printStackTrace();System.out.println("WISP_CLIENT_FAILED: "+e);mc.stop();}
 }
 private static double dynamic(net.minecraft.core.BlockPos pos)throws Exception {
  if(WispDynamicLights.backend().equals("none"))return 0;
  var c=Class.forName("org.thinkingstudio.ryoamiclights.RyoamicLights");
  return ((Number)c.getMethod("getDynamicLightLevel",net.minecraft.core.BlockPos.class).invoke(c.getMethod("get").invoke(null),pos)).doubleValue();
 }
}
