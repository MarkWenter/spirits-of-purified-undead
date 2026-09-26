package dev.purifiedundead.network;

import dev.purifiedundead.combat.GuardianMovementService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record GuardianActionPacket(Action action) {
    public enum Action { DOUBLE_JUMP, AIR_DASH }

    static void encode(GuardianActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    static GuardianActionPacket decode(FriendlyByteBuf buffer) {
        return new GuardianActionPacket(buffer.readEnum(Action.class));
    }

    static void handle(GuardianActionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer sender = context.getSender();
        if (sender != null) {
            GuardianMovementService.perform(sender, packet.action);
        }
        context.setPacketHandled(true);
    }
}
