package dev.purifiedundead.validation;

import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.entity.FerinEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.phys.Vec3;

/** Opt-in development world capture. Excluded from production artifacts. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class FerinDynamicClientSmoke {
    private static boolean loading;
    private static int ticks;
    private static FerinEntity slash;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END
                || !Boolean.getBoolean("purified_undead.ferinDynamicVisual")) return;
        var mc = Minecraft.getInstance();
        if (!loading && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            loading = true;
            mc.options.pauseOnLostFocus = false;
            mc.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
            mc.createWorldOpenFlows().loadLevel(mc.screen, "diary-validation");
            return;
        }
        if (mc.level == null || mc.player == null || mc.getOverlay() != null) return;
        ticks++;
        if (ticks > 20) {
            mc.player.setPos(0, 225, 0);
            mc.player.getAbilities().flying = true;
            mc.player.setDeltaMovement(Vec3.ZERO);
            mc.level.setRainLevel(0);
            mc.level.setThunderLevel(0);
        }
        if (ticks == 20) {
            mc.getSingleplayerServer()
                    .execute(
                            () -> {
                                var server = mc.getSingleplayerServer();
                                var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                                player.removeAllEffects();
                                player.teleportTo(server.overworld(), 0, 225, 0, -25, 20);
                                player.getAbilities().flying = true;
                                player.onUpdateAbilities();
                                server.overworld().setWeatherParameters(6000, 0, false, false);
                                server.overworld().setDayTime(18000);
                            });
        }
        if (ticks < 60) return;
        int stage = 1 + (ticks - 60) / 32;
        int frame = (ticks - 60) % 32;
        if (stage > 5) {
            mc.options.hideGui = false;
            if (dev.purifiedundead.content.ModItems.KNIGHT_NOCTURNE != null) {
                var stack = new net.minecraft.world.item.ItemStack(dev.purifiedundead.content.ModItems.KNIGHT_NOCTURNE.get());
                var model = mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);
                var gui = model.applyTransform(net.minecraft.world.item.ItemDisplayContext.GUI,new com.mojang.blaze3d.vertex.PoseStack(),false);
                var hand = model.applyTransform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,new com.mojang.blaze3d.vertex.PoseStack(),false);
                if (gui == hand || hand.getQuads(null,null,net.minecraft.util.RandomSource.create()).isEmpty())
                    throw new IllegalStateException("Knight Nocturne GUI or handheld model missing");
                System.out.println("NOCTURNE_CLIENT_OK: distinct GUI and three-dimensional hand model");
            }
            System.out.println("FERIN_DYNAMIC_CLIENT_OK: five timed launches and flights captured");
            mc.stop();
            return;
        }
        mc.options.hideGui = true;
        mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        mc.player.setYRot(-25);
        mc.player.setXRot(20);
        // A deterministic presentation fixture exercises the same renderer and synchronized data.
        // Server-side normal attack/flight behavior is covered separately by FerinFlightSmoke.
        if (frame == 0) {
            if (slash != null) slash.discard();
            slash = new FerinEntity(ModEntities.FERIN.get(), mc.level);
            slash.syncStage(stage, mc.level.getGameTime());
            slash.syncFlight(new Vec3(7, 220, 10), new Vec3(0, 0, 1), 24, 0, .35);
            mc.level.putNonPlayerEntity(slash.getId(), slash);
        } else if (frame <= 24) {
            slash.syncFlight(new Vec3(7, 220, 10 + frame * .35), new Vec3(0, 0, 1), 24, frame, .35);
        }
        if (frame < 29)
            net.minecraft.client.Screenshot.grab(
                    mc.gameDirectory,
                    "ferin-dynamic-" + stage + "-" + String.format("%02d", frame) + ".png",
                    mc.getMainRenderTarget(),
                    message -> {});
    }
}
