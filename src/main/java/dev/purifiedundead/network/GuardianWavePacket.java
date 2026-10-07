package dev.purifiedundead.network;
public record GuardianWavePacket(int shot) {
 public static void encode(GuardianWavePacket p,net.minecraft.network.FriendlyByteBuf b){b.writeVarInt(p.shot());}
 public static GuardianWavePacket decode(net.minecraft.network.FriendlyByteBuf b){return new GuardianWavePacket(b.readVarInt());}
 public static void handle(GuardianWavePacket p,java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier){var c=supplier.get();if(c.getSender()!=null)dev.purifiedundead.combat.GuardianWaveCombat.swing(c.getSender(),p.shot());c.setPacketHandled(true);}
}
