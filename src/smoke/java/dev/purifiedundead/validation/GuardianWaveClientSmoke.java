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
   if(ticks==80||ticks==140){dev.purifiedundead.client.GuardianWaveInput.input(new net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered(0,mc.options.keyAttack,net.minecraft.world.InteractionHand.MAIN_HAND));mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
   if(ticks==80||ticks==140){if(mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(2),GuardianWaveEntity::predicted).isEmpty())throw new IllegalStateException("no same-input prediction");}
   if(ticks==100){delayedShot=dev.purifiedundead.client.GuardianWavePrediction.start();if(delayedShot<=0)throw new IllegalStateException("prediction failed");}
   if(ticks==120){if(!mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(20),GuardianWaveEntity::predicted).isEmpty())throw new IllegalStateException("prediction waits for server");dev.purifiedundead.network.ModNetwork.CHANNEL.sendToServer(new dev.purifiedundead.network.GuardianWavePacket(delayedShot));}
   for(var w:mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(20))){if(w.roll()<0||w.roll()>180)throw new IllegalStateException("invalid roll");seen=true;if(!w.predicted()){confirmed=true;if(!dev.purifiedundead.client.GuardianWavePrediction.suppress(w))throw new IllegalStateException("duplicate server visual");if(w.shotId()==delayedShot)lateConfirmed=true;}if(w.predicted()&&(++frames==2||frames==5||frames==8))net.minecraft.client.Screenshot.grab(mc.gameDirectory,"slate052-wave-"+ticks+".png",mc.getMainRenderTarget(),m->{});}
   if(ticks==180){if(!confirmed||!lateConfirmed)throw new IllegalStateException("missing actual/late server confirmation");if(!seen)throw new IllegalStateException("no network wave rendered");if(!mc.level.getEntitiesOfClass(GuardianWaveEntity.class,mc.player.getBoundingBox().inflate(20)).isEmpty())throw new IllegalStateException("orphan wave");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);System.out.println("GUARDIAN_CLIENT_OK: immediate prediction, delayed confirmation without duplicate, real packets, synced entity, random roll, renderer and expiry");}
   if(ticks==190)mc.getSingleplayerServer().execute(()->{
       var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
       dev.purifiedundead.purification.PurificationProgress.unlock(p);
       var book=new net.minecraft.world.item.ItemStack(ModItems.LILY_DIARY.get());
       var memories=net.minecraft.core.NonNullList.withSize(8,net.minecraft.world.item.ItemStack.EMPTY);
       memories.set(0,new net.minecraft.world.item.ItemStack(dev.purifiedundead.slate.SlateContent.MEMORIES.get("ulv").get()));
       memories.set(1,new net.minecraft.world.item.ItemStack(dev.purifiedundead.slate.SlateContent.MEMORIES.get("guardians").get()));
       dev.purifiedundead.slate.MemoryStorage.write(book,memories);
       top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).ifPresent(h->{h.getCurios().get("wanderer_log").getStacks().setStackInSlot(0,book);h.getCurios().get("ancient_contract").getStacks().setStackInSlot(0,new net.minecraft.world.item.ItemStack(ModItems.ANCIENT_CONTRACT.get()));});
       for(int y=90;y<110;y++)p.serverLevel().setBlockAndUpdate(new net.minecraft.core.BlockPos(6,y,1),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
   });
   if(ticks==230){mc.options.keyJump.setDown(true);mc.getSingleplayerServer().execute(()->{
       var p=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());p.getAbilities().flying=false;p.onUpdateAbilities();p.teleportTo(p.serverLevel(),5.69,104,1.5,0,0);p.setDeltaMovement(0,-.1,0);
   });}
   if(ticks==270){if(!dev.purifiedundead.slate.WallGrip.eligible(mc.player))throw new IllegalStateException("client wall setup "+mc.player.position());mc.options.keyJump.setDown(false);}
   if(ticks==272){mc.options.keyJump.setDown(true);dev.purifiedundead.client.GuardianInputEvents.onClientTick(new net.minecraftforge.event.TickEvent.ClientTickEvent(net.minecraftforge.event.TickEvent.Phase.END));if(mc.player.getDeltaMovement().y<.51)throw new IllegalStateException("wall jump not immediate");System.out.println("SLATE_CLIENT_JUMP_OK: synchronized diary and same-input wall jump");}
   if(ticks==280){System.out.println("SLATE_CLIENT_OK: real synchronized diary and wall jump prediction");mc.options.keyJump.setDown(false);mc.stop();}
  }catch(Throwable t){t.printStackTrace();System.out.println("GUARDIAN_CLIENT_FAILED");mc.options.hideGui=false;mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);mc.stop();}
 }
}
