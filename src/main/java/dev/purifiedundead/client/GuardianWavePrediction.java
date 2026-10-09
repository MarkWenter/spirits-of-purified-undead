package dev.purifiedundead.client;

import dev.purifiedundead.content.*;
import dev.purifiedundead.entity.GuardianWaveEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Bounded local cosmetics; positive shot IDs identify locally predicted swings, even with late server confirmation. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GuardianWavePrediction {
    private static LocalPlayer previous;
    private static int sequence, visualId = -1000000000;
    private static long lastTick = Long.MIN_VALUE;

    private static void refresh() {
        var p = Minecraft.getInstance().player;
        if (previous != p) {
            lastTick = Long.MIN_VALUE;
            previous = p;
        }
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if (e.phase == net.minecraftforge.event.TickEvent.Phase.END) refresh();
    }

    public static int start() {
        refresh();
        var mc = Minecraft.getInstance();
        var p = mc.player;
        if (p == null
                || mc.level == null
                || !p.isAlive()
                || p.isSpectator()
                || p.isUsingItem()
                || p.hasEffect(ModEffects.STUNNED.get())
                || !p.getMainHandItem().is(ModItems.BLIGHTED_GUARDIAN.get())) return 0;
        long now = p.level().getGameTime();
        if (lastTick == now) return 0;
        lastTick = now;
        sequence = sequence == Integer.MAX_VALUE ? 1 : sequence + 1;
        var wave = ModEntities.GUARDIAN_WAVE.get().create(mc.level);
        if (wave == null) return 0;
        do {
            if (--visualId < Integer.MIN_VALUE + 1) visualId = -1000000000;
        } while (mc.level.getEntity(visualId) != null);
        wave.setId(visualId);
        wave.predict(p, sequence);
        mc.level.putNonPlayerEntity(wave.getId(), wave);
        return sequence;
    }

    public static boolean suppress(GuardianWaveEntity wave) {
        var p = Minecraft.getInstance().player;
        return !wave.predicted() && p != null && wave.ownerId() == p.getId() && wave.shotId() > 0;
    }

    private GuardianWavePrediction() {}
}
