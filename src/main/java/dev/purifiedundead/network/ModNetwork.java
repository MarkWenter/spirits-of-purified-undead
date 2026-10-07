package dev.purifiedundead.network;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "9";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(GuardianWavePacket.class,4,NetworkDirection.PLAY_TO_SERVER).encoder(GuardianWavePacket::encode).decoder(GuardianWavePacket::decode).consumerMainThread(GuardianWavePacket::handle).add();
        CHANNEL.messageBuilder(FoundryRecipesPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT).encoder(FoundryRecipesPacket::encode).decoder(FoundryRecipesPacket::decode).consumerMainThread(FoundryRecipesPacket::handle).add();
        CHANNEL.messageBuilder(GuardianMotionSettingsPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(GuardianMotionSettingsPacket::encode).decoder(GuardianMotionSettingsPacket::decode)
                .consumerMainThread(GuardianMotionSettingsPacket::handle).add();
        CHANNEL.messageBuilder(FerinContinuePacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FerinContinuePacket::encode).decoder(FerinContinuePacket::decode)
                .consumerMainThread(FerinContinuePacket::handle).add();
        CHANNEL.messageBuilder(GuardianActionPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GuardianActionPacket::encode)
                .decoder(GuardianActionPacket::decode)
                .consumerMainThread(GuardianActionPacket::handle)
                .add();
    }
}
