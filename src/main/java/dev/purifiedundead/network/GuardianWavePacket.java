package dev.purifiedundead.network;
public record GuardianWavePacket() {
 public static void encode(GuardianWavePacket p,net.minecraft.network.FriendlyByteBuf b){}
 public static GuardianWavePacket decode(net.minecraft.network.FriendlyByteBuf b){return new GuardianWavePacket();}
 public static void handle(GuardianWavePacket p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();if(c.getSender()!=null)dev.purifiedundead.combat.GuardianWaveCombat.swing(c.getSender());c.setPacketHandled(true);}
}
