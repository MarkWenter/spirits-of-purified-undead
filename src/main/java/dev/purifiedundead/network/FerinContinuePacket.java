package dev.purifiedundead.network;
import dev.purifiedundead.combat.FerinCombatEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
/** Intent only: no client-provided damage, stage, target or position. */
public record FerinContinuePacket() {
 static void encode(FerinContinuePacket packet,FriendlyByteBuf buffer) { }
 static FerinContinuePacket decode(FriendlyByteBuf buffer){return new FerinContinuePacket();}
 static void handle(FerinContinuePacket packet,Supplier<NetworkEvent.Context> supplier){
  var context=supplier.get();var player=context.getSender();
  if(player!=null)MinecraftForge.EVENT_BUS.post(new FerinCombatEvents.ContinueInput(player));
  context.setPacketHandled(true);
 }
}
