package dev.purifiedundead.client;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="purified_undead",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GuardianWaveInput {
 @net.minecraftforge.eventbus.api.SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST)
 public static void input(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered e){
  var p=net.minecraft.client.Minecraft.getInstance().player;
  if(e.isCanceled()||!e.isAttack()||p==null||!p.getMainHandItem().is(dev.purifiedundead.content.ModItems.BLIGHTED_GUARDIAN.get()))return;
  int shot=GuardianWavePrediction.start();if(shot==0)return;
  dev.purifiedundead.network.ModNetwork.CHANNEL.sendToServer(new dev.purifiedundead.network.GuardianWavePacket(shot));
 }
}
