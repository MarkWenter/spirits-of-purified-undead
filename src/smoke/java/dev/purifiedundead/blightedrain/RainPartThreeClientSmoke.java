package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.content.*;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class RainPartThreeClientSmoke {
    private static boolean opened, started;
    private static volatile boolean ready;
    private static int ticks;
    private static final String[] NAMES = {
        "still_rain_beacon",
        "blighted_rain_emblem",
        "grave_mire_dust",
        "blighted_ruby",
        "blighted_meat",
        "cooked_blighted_meat",
        "cliff_village_youth",
        "headless_knight"
    };

    private static ItemStack stack(String name) {
        return new ItemStack(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                "purified_undead", name)));
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!Boolean.getBoolean("purified_undead.rainPart3Client")) return;
        var mc = Minecraft.getInstance();
        mc.options.pauseOnLostFocus = false;
        try {
            if (!opened
                    && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen
                    && mc.getOverlay() == null) {
                opened = true;
                mc.createWorldOpenFlows().loadLevel(mc.screen, "blighted-rain-visual-test");
                return;
            }
            if (mc.level == null || mc.player == null || mc.getOverlay() != null) return;
            if (!started) {
                started = true;
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                mc.options.hideGui = false;
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    var level = mc.getSingleplayerServer().overworld();
                                    var p =
                                            mc.getSingleplayerServer()
                                                    .getPlayerList()
                                                    .getPlayers()
                                                    .get(0);
                                    p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                                    p.getAbilities().flying = true;
                                    p.onUpdateAbilities();
                                    p.connection.teleport(44.5, 150.2, 48.3, 180, 12);
                                    p.removeAllEffects();
                                    level.setDayTime(6000);
                                    BlightedRainData.get(level).setWeatherLocked(false);
                                    RainController.stop(level);
                                    level.setWeatherParameters(12000, 0, false, false);
                                    for (var e :
                                            level.getEntitiesOfClass(
                                                    net.minecraft.world.entity.Mob.class,
                                                    new net.minecraft.world.phys.AABB(
                                                            39, 148, 39, 52, 158, 53))) e.discard();
                                    var pos = new net.minecraft.core.BlockPos(44, 150, 44);
                                    level.setBlockAndUpdate(
                                            pos.below(),
                                            net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK
                                                    .defaultBlockState());
                                    level.setBlockAndUpdate(
                                            pos, RainItems.BEACON_BLOCK.get().defaultBlockState());
                                    level.setBlockAndUpdate(
                                            pos.east(),
                                            net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK
                                                    .defaultBlockState());
                                    for (int i = 0; i < NAMES.length; i++)
                                        p.getInventory().setItem(i, stack(NAMES[i]));
                                    p.getInventory().selected = 7;
                                    var projectile =
                                            new CliffYouthProjectile(
                                                    RainItems.YOUTH_PROJECTILE.get(), level);
                                    projectile.setPos(43.4, 151, 44.5);
                                    projectile.setNoGravity(true);
                                    level.addFreshEntity(projectile);
                                    ready = true;
                                });
            }
            if (!ready) return;
            if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)
                mc.setScreen(null);
            ticks++;
            if (ticks < 120) mc.player.getInventory().selected = 7;
            if (ticks == 80) shot(mc, "rain-part3-world.png");
            if (ticks == 90) {
                mc.options.keyUse.setDown(true);
                mc.player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
                mc.getSingleplayerServer()
                        .execute(
                                () ->
                                        mc.getSingleplayerServer()
                                                .getPlayerList()
                                                .getPlayers()
                                                .get(0)
                                                .startUsingItem(
                                                        net.minecraft.world.InteractionHand
                                                                .MAIN_HAND));
            }
            if (ticks == 110) { if (!mc.player.isBlocking()) throw new IllegalStateException("Shield not blocking"); shot(mc, "rain-part3-blocking.png"); }
            if (ticks == 120) {
                mc.options.keyUse.setDown(false);
                for (String name : NAMES) {
                    if (!name.equals("still_rain_beacon")
                            && mc.getResourceManager()
                                    .getResource(
                                            net.minecraft.resources.ResourceLocation
                                                    .fromNamespaceAndPath(
                                                            "purified_undead",
                                                            "textures/item/" + name + ".png"))
                                    .isEmpty())
                        throw new IllegalStateException("Missing texture " + name);
                    var s = stack(name);
                    if (s.isEmpty()
                            || mc.getItemRenderer().getModel(s, mc.level, mc.player, 0)
                                    == mc.getModelManager().getMissingModel())
                        throw new IllegalStateException("Missing item model " + name);
                }
                mc.setScreen(new Gallery());
            }
            if (ticks == 155) shot(mc, "rain-part3-items.png");
            if (ticks == 175) {
                System.out.println(
                        "RAIN_PART3_CLIENT_OK eight item models, beacon, shield held/blocking,"
                            + " half-block projectile; screenshots saved");
                mc.stop();
            }
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("RAIN_PART3_CLIENT_FAILED " + error);
            mc.stop();
        }
    }

    private static void shot(Minecraft mc, String name) {
        net.minecraft.client.Screenshot.grab(
                mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
    }

    private static final class Gallery extends net.minecraft.client.gui.screens.Screen {
        Gallery() {
            super(net.minecraft.network.chat.Component.literal("Blighted Rain Part III"));
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }

        @Override
        public void render(
                net.minecraft.client.gui.GuiGraphics graphics, int mx, int my, float partial) {
            graphics.fill(0, 0, width, height, 0xff242329);
            graphics.drawCenteredString(font, title, width / 2, 12, 0xffffff);
            for (int i = 0; i < NAMES.length; i++) {
                int x = width / 2 - 160 + (i % 4) * 85, y = 45 + (i / 4) * 100;
                var item = stack(NAMES[i]);
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(3, 3, 3);
                graphics.renderItem(item, 0, 0);
                graphics.pose().popPose();
                graphics.drawCenteredString(
                        font,
                        net.minecraft.network.chat.Component.literal("" + (i + 1)),
                        x + 24,
                        y + 55,
                        0xffffff);
            }
        }
    }
}
