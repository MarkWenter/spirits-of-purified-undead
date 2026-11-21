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
    private static int echoStages;
    private static final dev.purifiedundead.client.echo.EchoAnimations.Motion PROBE =
            new dev.purifiedundead.client.echo.EchoAnimations.Motion() {
                private boolean classic;
                public boolean matches(dev.purifiedundead.client.echo.EchoAnimations.Skeleton rig) {
                    classic = dev.purifiedundead.client.echo.EchoAnimations.wineFoxClassic(rig);
                    return classic || dev.purifiedundead.client.echo.EchoAnimations.wineFoxGecko(rig);
                }
                public boolean sample(int stage, float age, dev.purifiedundead.client.echo.EchoAnimations.Rotations pose) {
                    if (classic != (stage == 1 || stage == 4))
                        throw new AssertionError("Unexpected Wine Fox rig on stage " + stage);
                    pose.rotate(classic ? "armRight" : "RightForeArm", .1F, 0, 0);
                    echoStages |= 1 << stage;
                    return true;
                }
            };
    private static int ticks;
    private static FerinEntity slash;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END
                || !Boolean.getBoolean("purified_undead.ferinDynamicVisual")) return;
        var mc = Minecraft.getInstance();
        if (!loading && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
            loading = true;
            dev.purifiedundead.client.echo.EchoAnimations.register(PROBE);
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
                if ((stage == 6 || stage == 7) && frame == 0) {
                    var server = mc.getSingleplayerServer();
                    var uuid = mc.player.getUUID();
                    var held = stage == 6 ? new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL) : stack;
                    server.execute(() -> {
                        var player = server.getPlayerList().getPlayer(uuid);
                        if (player != null) player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, held);
                        server.overworld().setDayTime(9000);
                    });
                }
                if (stage <= 7 && (frame == 20 || stage == 7 && frame == 29)) {
                    String capture = stage == 6 ? "nocturne-flint" : "nocturne-wisps-" + frame;
                    net.minecraft.client.Screenshot.grab(mc.gameDirectory, capture + ".png",
                            mc.getMainRenderTarget(), message -> {});
                }
                if (stage <= 7) return;
                var model = mc.getItemRenderer().getModel(stack,mc.level,mc.player,0);
                var gui = model.applyTransform(net.minecraft.world.item.ItemDisplayContext.GUI,new com.mojang.blaze3d.vertex.PoseStack(),false);
                var hand = model.applyTransform(net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,new com.mojang.blaze3d.vertex.PoseStack(),false);
                if (gui == hand || hand.getQuads(null,null,net.minecraft.util.RandomSource.create()).isEmpty())
                    throw new IllegalStateException("Knight Nocturne GUI or handheld model missing");
                int expectedStages = Boolean.getBoolean("purified_undead.ysmEchoSmoke") ? 46 : 62;
                if (echoStages != expectedStages) throw new AssertionError("Maid echo stage mask " + echoStages);
                System.out.println("NOCTURNE_ECHO_CLIENT_OK: matched live poses across classic, Gecko and alternate texture");
                dev.purifiedundead.client.echo.EchoAnimations.unregister(PROBE);
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
            if (dev.purifiedundead.content.ModItems.KNIGHT_NOCTURNE != null) {
                var appearance = new net.minecraft.nbt.CompoundTag();
                appearance.putString("model", stage == 1 || stage == 4 ? "authors_and_credits:wine_fox_maid"
                        : stage == 3 ? "geckolib:winefox_blue" : "geckolib:winefox");
                if (stage == 4 && Boolean.getBoolean("purified_undead.ysmEchoSmoke")) {
                    appearance.putString("ysmModel", "wine_fox/01_taisho_maid");
                    appearance.putString("ysmTexture", "skin_white");
                }
                slash.setEchoAppearance(appearance);
            }
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
