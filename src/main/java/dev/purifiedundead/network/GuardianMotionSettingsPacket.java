package dev.purifiedundead.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record GuardianMotionSettingsPacket(double jump, double dash) {
    static void encode(GuardianMotionSettingsPacket p, FriendlyByteBuf b) {
        b.writeDouble(p.jump);
        b.writeDouble(p.dash);
    }

    static GuardianMotionSettingsPacket decode(FriendlyByteBuf b) {
        return new GuardianMotionSettingsPacket(b.readDouble(), b.readDouble());
    }

    static void handle(GuardianMotionSettingsPacket p, Supplier<NetworkEvent.Context> supplier) {
        dev.purifiedundead.client.GuardianInputEvents.setServerMotion(p.jump, p.dash);
        supplier.get().setPacketHandled(true);
    }
}
