package dev.purifiedundead.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import dev.purifiedundead.network.GuardianActionPacket;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AbilityKeys {
    public static final KeyMapping FERIN =
            new KeyMapping(
                    "key.purified_undead.toggle_ferin",
                    InputConstants.UNKNOWN.getValue(),
                    "key.categories.purified_undead");
    public static final KeyMapping MOVEMENT =
            new KeyMapping(
                    "key.purified_undead.toggle_guardian_movement",
                    InputConstants.UNKNOWN.getValue(),
                    "key.categories.purified_undead");
    public static boolean ferinEnabled = true, movementEnabled = true;

    private AbilityKeys() {}

    public static void set(boolean ferin, boolean movement) {
        ferinEnabled = ferin;
        movementEnabled = movement;
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        var mc = net.minecraft.client.Minecraft.getInstance();
        boolean ferin = false, movement = false;
        while (FERIN.consumeClick()) ferin = true;
        while (MOVEMENT.consumeClick()) movement = true;
        if (mc.player == null) {
            set(true, true);
            return;
        }
        if (mc.screen != null || mc.isPaused()) return;
        if (ferin)
            dev.purifiedundead.network.ModNetwork.CHANNEL.sendToServer(
                    new GuardianActionPacket(GuardianActionPacket.Action.TOGGLE_FERIN));
        if (movement)
            dev.purifiedundead.network.ModNetwork.CHANNEL.sendToServer(
                    new GuardianActionPacket(GuardianActionPacket.Action.TOGGLE_GUARDIAN_MOVEMENT));
    }

    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(
            modid = "purified_undead",
            bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
            value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class Registration {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void register(
                net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
            event.register(FERIN);
            event.register(MOVEMENT);
        }
    }
}
