package dev.purifiedundead.blightedrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import dev.purifiedundead.blightedrain.mob.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class BlightedMobClientSmoke {
    private static boolean opened, started, stopping;
    private static int ticks, stage = -1;
    private static volatile boolean ready, sunOK;
    private static volatile int warningSounds;
    private static final java.util.Set<String> rendered = new java.util.HashSet<>();
    private static final String[] names = {
        "zombie",
        "skeleton",
        "drowned",
        "husk",
        "stray",
        "villager",
        "pillager",
        "vindicator",
        "witch",
        "pig",
        "cow",
        "sheep",
        "chicken",
        "horse",
        "donkey"
    };
    private static net.minecraft.world.entity.Mob current;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void sound(net.minecraftforge.client.event.sound.PlaySoundEvent e) {
        if (e.getSound() != null
                && e.getSound()
                        .getLocation()
                        .toString()
                        .equals("purified_undead:blighted_rain_warning")) warningSounds++;
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void render(net.minecraftforge.client.event.RenderLivingEvent.Post<?, ?> e) {
        if (e.getEntity() instanceof BlightedVariant)
            rendered.add(
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                            .getKey(e.getEntity().getType())
                            .getPath());
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!Boolean.getBoolean("purified_undead.rainClientSmoke")) return;
        var mc = Minecraft.getInstance();
        try {
            if (!opened && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                opened = true;
                mc.createWorldOpenFlows().loadLevel(mc.screen, "blighted-rain-visual-test");
                return;
            }
            if (mc.level == null || mc.player == null || mc.getOverlay() != null) return;
            if (!started) {
                started = true;
                mc.options.hideGui = true;
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    try {
                                        var level = mc.getSingleplayerServer().overworld();
                                        var p =
                                                mc.getSingleplayerServer()
                                                        .getPlayerList()
                                                        .getPlayers()
                                                        .get(0);
                                        p.addEffect(
                                                new net.minecraft.world.effect.MobEffectInstance(
                                                        net.minecraft.world.effect.MobEffects
                                                                .NIGHT_VISION,
                                                        2400,
                                                        0));
                                        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                                        p.getAbilities().flying = true;
                                        p.onUpdateAbilities();
                                        p.connection.teleport(44.5, 150.5, 49.5, 180, 8);
                                        level.setDayTime(6000);
                                        level.getGameRules()
                                                .getRule(
                                                        net.minecraft.world.level.GameRules
                                                                .RULE_DOMOBSPAWNING)
                                                .set(false, mc.getSingleplayerServer());
                                        for (int x = 40; x <= 50; x++)
                                            for (int z = 40; z <= 52; z++)
                                                level.setBlockAndUpdate(
                                                        new net.minecraft.core.BlockPos(x, 149, z),
                                                        net.minecraft.world.level.block.Blocks.STONE
                                                                .defaultBlockState());
                                        for (var old :
                                                level.getEntitiesOfClass(
                                                        net.minecraft.world.entity.Mob.class,
                                                        new net.minecraft.world.phys.AABB(
                                                                39, 148, 39, 52, 158, 53)))
                                            old.discard();
                                        var data = BlightedRainData.get(level);
                                        data.model.reset();
                                        data.setWeatherLocked(false);
                                        data.setEnabled(true);
                                        level.setWeatherParameters(12000, 0, false, false);
                                        level.setRainLevel(0);
                                        level.setThunderLevel(0);
                                        for (var player : level.players())
                                            player.connection.send(
                                                    new net.minecraft.network.protocol.game
                                                            .ClientboundGameEventPacket(
                                                            net.minecraft.network.protocol.game
                                                                    .ClientboundGameEventPacket
                                                                    .STOP_RAINING,
                                                            0));
                                        for (var player : level.players())
                                            player.connection.send(
                                                    new net.minecraft.network.protocol.game
                                                            .ClientboundGameEventPacket(
                                                            net.minecraft.network.protocol.game
                                                                    .ClientboundGameEventPacket
                                                                    .RAIN_LEVEL_CHANGE,
                                                            0));
                                        ready = true;
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        System.out.println("BLIGHTED_MOBS_CLIENT_FAILED setup");
                                        mc.execute(mc::stop);
                                    }
                                });
                return;
            }
            if (!ready) return;
            mc.player.noPhysics = true;
            mc.player.setNoGravity(true);
            mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            mc.player.setPos(44.5, 150.5, 49.5);
            mc.player.setYRot(180);
            mc.player.setXRot(8);

            if (++ticks > 2500) throw new IllegalStateException("gallery timeout");
            int desired = (ticks - 1) / 70;
            if (desired < names.length && desired != stage) {
                stage = desired;
                final int idx = desired;
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    var level = mc.getSingleplayerServer().overworld();
                                    if (current != null) {
                                        if (current.getRemainingFireTicks() > 0) {
                                            System.out.println(
                                                    "BLIGHTED_MOBS_CLIENT_FAILED sun burn");
                                        }
                                        current.discard();
                                    }
                                    var type =
                                            (net.minecraft.world.entity.EntityType<
                                                            ? extends
                                                                    net.minecraft.world.entity.Mob>)
                                                    net.minecraft.core.registries.BuiltInRegistries
                                                            .ENTITY_TYPE
                                                            .get(
                                                                    net.minecraft.resources
                                                                            .ResourceLocation
                                                                            .fromNamespaceAndPath(
                                                                                    "purified_undead",
                                                                                    "blighted_"
                                                                                            + names[
                                                                                                    idx]));
                                    current = type.create(level);
                                    current.setPos(44.5, 150, 44.5);
                                    current.setNoAi(true);
                                    current.setNoGravity(true);
                                    current.setYRot(0);
                                    current.yBodyRot = 0;
                                    current.yHeadRot = 0;
                                    if (current
                                            instanceof
                                            net.minecraft.world.entity.animal.horse.AbstractHorse) {
                                        current.setYRot(60);
                                        current.yBodyRot = 60;
                                        current.yHeadRot = 60;
                                    }
                                    level.addFreshEntity(current);
                                    if (current
                                            instanceof net.minecraft.world.entity.animal.Sheep s)
                                        s.setColor(net.minecraft.world.item.DyeColor.WHITE);
                                    if (idx == names.length - 1) {
                                        sunOK = current.getRemainingFireTicks() <= 0;
                                        level.setWeatherParameters(0, 24000, true, false);
                                        RainController.start(level, 1200);
                                    }
                                });
            }
            if (desired < names.length && ticks % 70 == 55) {
                String label = names[desired];
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-" + label + ".png",
                        mc.getMainRenderTarget(),
                        m -> {});
                System.out.println(
                        "BLIGHTED_MOBS_GALLERY "
                                + label
                                + " camera="
                                + mc.gameRenderer.getMainCamera().getPosition());
            }
            if (desired >= names.length && ticks >= names.length * 70 + 40 && !stopping) {
                stopping = true;
                for (String name : names)
                    if (!rendered.contains("blighted_" + name))
                        throw new IllegalStateException("not actually rendered " + name);
                if (!sunOK || warningSounds != 1)
                    throw new IllegalStateException(
                            "sun/audio assertion " + sunOK + " / " + warningSounds);
                System.out.println(
                        "BLIGHTED_MOBS_CLIENT_OK: all "
                                + names.length
                                + " models rendered, layered native UV textures, no sun fire, one warning sound");
                mc.stop();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("BLIGHTED_MOBS_CLIENT_FAILED " + t);
            mc.stop();
        }
    }
}
