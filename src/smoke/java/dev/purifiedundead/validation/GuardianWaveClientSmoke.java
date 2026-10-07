package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.entity.GuardianWaveEntity;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GuardianWaveClientSmoke {
 private static boolean loading,seen;private static int ticks,frames;
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
  if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||!Boolean.getBoolean("purified_undead.guardianClient"))return;
  var mc=Minecraft.getInstance();
  try{
   if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
   if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;ticks++;
   if(ticks==40)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(mc.getSingleplayerServer().overworld(),5,100,1,0,0);p.getInventory().selected=0;p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(ModItems.BLIGHTED_GUARDIAN.get()));});
   if(ticks==65){mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);mc.options.hideGui=true;}
   if(ticks==80||ticks==100||ticks==120){dev.purifiedundead.client.GuardianWaveInput.input(new net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,net.minecraft.world.InteractionHand.MAIN_HAND));mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
   for(var w:mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(6))){if(w.roll()<0||w.roll()>180)throw new IllegalStateException("invalid roll");seen=true;if(++frames==2||frames==5||frames==8)net.minecraft.client.Screenshot.grab(mc.gameDirectory,"guardian-wave-"+ticks+".png",mc.getMainRenderTarget(),m->{});}
   if(ticks==150){if(!seen)throw new IllegalStateException("no network wave rendered");if(!mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(6)).isEmpty())throw new IllegalStateException("orphan wave");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);System.out.println("GUARDIAN_CLIENT_OK: real input packet, synced entity, random roll, renderer and expiry");mc.stop();}
  }catch(Throwable t){t.printStackTrace();System.out.println("GUARDIAN_CLIENT_FAILED");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);mc.stop();}
 }
}
