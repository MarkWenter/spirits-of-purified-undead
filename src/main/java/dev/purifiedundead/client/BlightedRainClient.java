package dev.purifiedundead.client;

import dev.purifiedundead.blightedrain.RainExposure;
import dev.purifiedundead.network.BlightedRainPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/** Server-synchronized presentation only. No player effects, weather or damage are decided here. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class BlightedRainClient {
    private static int phase;
    private static long end;
    private static float warningBase;

    private BlightedRainClient() {}

    public static void accept(BlightedRainPacket packet) {
        var level = Minecraft.getInstance().level;
        if (level == null || level.dimension() != Level.OVERWORLD) {
            clear();
            return;
        }
        phase = packet.phase();
        end = level.getGameTime() + packet.remaining();
        warningBase = packet.remaining();
    }

    public static void clear() {
        phase = 0;
        end = 0;
        warningBase = 0;
    }

    public static float intensity() {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null
                || level.dimension() != Level.OVERWORLD
                || !level.isRaining()
                || !RainExposure.precipitation(
                        level, mc.gameRenderer.getMainCamera().getBlockPosition())) return 0;
        if (phase == 2) return level.getGameTime() < end ? 1 : 0;
        if (phase == 1) return Math.max(0, Math.min(1, 1 - (end - level.getGameTime()) / 300F));
        return 0;
    }

    public static float tint(float component, int channel) {
        float f = intensity();
        float target = channel == 0 ? .65F : channel == 1 ? .48F : .47F;
        return component * (1 - f) + target * component * f;
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void disconnect(
            net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void fog(net.minecraftforge.client.event.ViewportEvent.ComputeFogColor event) {
        float f = intensity() * .3F;
        event.setRed(event.getRed() * (1 - f) + .45F * f);
        event.setGreen(event.getGreen() * (1 - f) + .32F * f);
        event.setBlue(event.getBlue() * (1 - f) + .32F * f);
    }
}
