package dev.purifiedundead.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record AbilitySettingsPacket(boolean ferin, boolean movement) {
    static void encode(AbilitySettingsPacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.ferin);
        b.writeBoolean(p.movement);
    }

    static AbilitySettingsPacket decode(FriendlyByteBuf b) {
        return new AbilitySettingsPacket(b.readBoolean(), b.readBoolean());
    }

    static void handle(AbilitySettingsPacket p, Supplier<NetworkEvent.Context> supplier) {
        dev.purifiedundead.client.AbilityKeys.set(p.ferin, p.movement);
        supplier.get().setPacketHandled(true);
    }
}
