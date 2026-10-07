package dev.purifiedundead.validation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.entity.GuardianWaveEntity;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GuardianWaveClientSmoke {
 private static boolean loading,seen,confirmed,lateConfirmed;private static int delayedShot;private static int ticks,frames;
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
  if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END||!Boolean.getBoolean("purified_undead.guardianClient"))return;
  var mc=Minecraft.getInstance();
  try{
   if(!loading&&mc.screen instanceof TitleScreen&&mc.getOverlay()==null){loading=true;mc.createWorldOpenFlows().loadLevel(mc.screen,"diary-validation");return;}
   if(mc.level==null||mc.player==null||mc.getOverlay()!=null)return;ticks++;
   if(ticks==40)mc.getSingleplayerServer().execute(()->{var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(mc.getSingleplayerServer().overworld(),5,100,1,0,0);p.getInventory().selected=0;p.getInventory().setItem(0,new net.minecraft.world.item.ItemStack(ModItems.BLIGHTED_GUARDIAN.get()));});
   if(ticks==65){mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.options.hideGui=true;}
   if(ticks==80||ticks==120){dev.purifiedundead.client.GuardianWaveInput.input(new net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,net.minecraft.world.InteractionHand.MAIN_HAND));mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
   if(ticks==80||ticks==120){if(mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(2),GuardianWaveEntity::predicted).isEmpty())throw new IllegalStateException("no same-input prediction");}
   if(ticks==100){delayedShot=dev.purifiedundead.client.GuardianWavePrediction.start();if(delayedShot<=0)throw new IllegalStateException("prediction failed");}
   if(ticks==110){if(!mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(10),GuardianWaveEntity::predicted).isEmpty())throw new IllegalStateException("prediction waits for server");dev.purifiedundead.network.ModNetwork.CHANNEL.sendToServer(new dev.purifiedundead.network.GuardianWavePacket(delayedShot));}
   for(var w:mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(10))){if(w.roll()<0||w.roll()>180)throw new IllegalStateException("invalid roll");seen=true;if(!w.predicted()){confirmed=true;if(!dev.purifiedundead.client.GuardianWavePrediction.suppress(w))throw new IllegalStateException("duplicate server visual");if(w.shotId()==delayedShot)lateConfirmed=true;}if(w.predicted()&&(++frames==2||frames==5||frames==8))net.minecraft.client.Screenshot.grab(mc.gameDirectory,"guardian049-wave-"+ticks+".png",mc.getMainRenderTarget(),m->{});}
   if(ticks==150){if(!confirmed||!lateConfirmed)throw new IllegalStateException("missing actual/late server confirmation");if(!seen)throw new IllegalStateException("no network wave rendered");if(!mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(10)).isEmpty())throw new IllegalStateException("orphan wave");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);System.out.println("GUARDIAN_CLIENT_OK: immediate prediction, delayed confirmation without duplicate, real packets, synced entity, random roll, renderer and expiry");mc.stop();}
  }catch(Throwable t){t.printStackTrace();System.out.println("GUARDIAN_CLIENT_FAILED");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);mc.stop();}
 }
}
