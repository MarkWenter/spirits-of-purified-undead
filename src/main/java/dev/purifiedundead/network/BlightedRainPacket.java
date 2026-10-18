package dev.purifiedundead.network;

import net.minecraft.network.FriendlyByteBuf;

public record BlightedRainPacket(int phase, int remaining) {
    public BlightedRainPacket {
        if (phase < 0 || phase > 3 || remaining < 0 || remaining > 18000)
            throw new IllegalArgumentException("Invalid rain state");
    }

    static void encode(BlightedRainPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.phase);
        buffer.writeVarInt(packet.remaining);
    }

    static BlightedRainPacket decode(FriendlyByteBuf buffer) {
        return new BlightedRainPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    static void handle(
            BlightedRainPacket p,
            java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> c) {
        dev.purifiedundead.client.BlightedRainClient.accept(p);
        c.get().setPacketHandled(true);
    }
}
