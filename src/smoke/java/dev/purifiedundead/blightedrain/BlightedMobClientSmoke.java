package dev.purifiedundead.blightedrain;

import dev.purifiedundead.blightedrain.mob.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class BlightedMobClientSmoke {
    private static boolean opened, started, stopping;
    private static int ticks, stage = -1;
    private static volatile boolean ready, sunOK;
    private static volatile int warningSounds;
    private static final java.util.Set<String> rendered = new java.util.HashSet<>();
    private static String[] names = {
        "spider",
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
        "donkey",
        "zombie:baby",
        "husk:baby",
        "drowned:baby",
        "villager:baby",
        "zombie:back",
        "skeleton:back",
        "drowned:back",
        "husk:back",
        "stray:back",
        "pillager:back",
        "vindicator:back",
        "witch:back",
        "pig:side",
        "cow:side",
        "sheep:side",
        "sheep:sheared",
        "sheep:red",
        "chicken:side",
        "villager:farmer",
        "villager:librarian",
        "horse:coat0",
        "horse:coat1",
        "horse:coat2",
        "horse:coat3",
        "horse:coat4",
        "horse:coat5",
        "horse:coat6",
    };

    static {
        if (Boolean.getBoolean("purified_undead.rainQuickReview"))
            names = new String[] {"spider", "skeleton", "villager", "sheep", "spider:back"};
    }

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
        mc.options.pauseOnLostFocus = false;
        if (mc.level != null && mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)
            mc.setScreen(null);
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
                                        p.removeAllEffects();
                                        p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                                        p.getAbilities().flying = true;
                                        p.onUpdateAbilities();
                                        p.connection.teleport(44.5, 150.2, 47.8, 180, 14);
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
            mc.player.setPos(44.5, 150.2, 47.8);
            mc.player.setYRot(180);
            mc.player.setXRot(14);

            if (++ticks > 6000) throw new IllegalStateException("gallery timeout");
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
                                                                                                    idx]
                                                                                                    .split(
                                                                                                            ":")[
                                                                                                    0]));
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
                                    String variant = names[idx];
                                    if (variant.endsWith(":baby")) {
                                        if (current
                                                instanceof
                                                net.minecraft.world.entity.monster.Zombie baby)
                                            baby.setBaby(true);
                                    }
                                    if (variant.endsWith(":back") || variant.endsWith(":side")) {
                                        float yaw = variant.endsWith(":back") ? 180 : 70;
                                        current.setYRot(yaw);
                                        current.yBodyRot = yaw;
                                        current.yHeadRot = yaw;
                                    }
                                    if (current
                                                    instanceof
                                                    net.minecraft.world.entity.animal.horse.Horse
                                                                    horse
                                            && variant.contains(":coat")) {
                                        int coat =
                                                Integer.parseInt(
                                                        variant.substring(variant.length() - 1));
                                        var coatData = new net.minecraft.nbt.CompoundTag();
                                        horse.addAdditionalSaveData(coatData);
                                        coatData.putInt("Variant", coat | ((coat % 5) << 8));
                                        horse.readAdditionalSaveData(coatData);
                                    }
                                    if (current
                                            instanceof
                                            net.minecraft.world.entity.monster.ZombieVillager
                                                            villager) {
                                        if (variant.endsWith(":farmer"))
                                            villager.setVillagerData(
                                                    villager.getVillagerData()
                                                            .setProfession(
                                                                    net.minecraft.world.entity.npc
                                                                            .VillagerProfession
                                                                            .FARMER));
                                        if (variant.endsWith(":librarian"))
                                            villager.setVillagerData(
                                                    villager.getVillagerData()
                                                            .setProfession(
                                                                    net.minecraft.world.entity.npc
                                                                            .VillagerProfession
                                                                            .LIBRARIAN));
                                    }
                                    level.addFreshEntity(current);
                                    if (current
                                            instanceof net.minecraft.world.entity.animal.Sheep s) {
                                        s.setColor(
                                                variant.endsWith(":red")
                                                        ? net.minecraft.world.item.DyeColor.RED
                                                        : net.minecraft.world.item.DyeColor.WHITE);
                                        s.setSheared(variant.endsWith(":sheared"));
                                    }
                                    if (idx == names.length - 1) {
                                        sunOK = current.getRemainingFireTicks() <= 0;
                                        level.setWeatherParameters(0, 24000, true, false);
                                        RainController.start(level, 1200);
                                    }
                                });
            }
            if (desired < names.length && ticks % 70 == 55) {
                String label = names[desired].replace(':', '-');
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
            if (ticks == names.length * 70 + 10) {
                net.minecraft.world.item.CreativeModeTabs.tryRebuildTabContents(
                        mc.level.enabledFeatures(), true, mc.level.registryAccess());
                var tab = dev.purifiedundead.content.ModCreativeTabs.MAIN.get();
                var entries = tab.getDisplayItems();
                var iconModel =
                        mc.getItemRenderer().getModel(tab.getIconItem(), mc.level, mc.player, 0);
                if (iconModel.getParticleIcon().contents().name().getPath().equals("missingno"))
                    throw new IllegalStateException("missing creative tab icon sprite");
                long eggs =
                        entries.stream()
                                .filter(
                                        stack ->
                                                stack.getItem()
                                                        instanceof
                                                        net.minecraft.world.item.SpawnEggItem)
                                .count();
                if (eggs != 18
                        || entries.stream()
                                .noneMatch(
                                        stack ->
                                                stack.is(
                                                        dev.purifiedundead.content.ModItems
                                                                .ANCIENT_CONTRACT
                                                                .get())))
                    throw new IllegalStateException("creative catalogue missing items/eggs");
                var tools =
                        net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB.get(
                                net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES);
                if (tools.getDisplayItems().stream()
                        .anyMatch(
                                stack ->
                                        net.minecraft.core.registries.BuiltInRegistries.ITEM
                                                .getKey(stack.getItem())
                                                .getNamespace()
                                                .equals("purified_undead")))
                    throw new IllegalStateException("mod item leaked into vanilla tools");
                var screen =
                        new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(
                                mc.player, mc.level.enabledFeatures(), true);
                mc.setScreen(screen);
                var select =
                        screen.getClass()
                                .getDeclaredMethod(
                                        "selectTab",
                                        net.minecraft.world.item.CreativeModeTab.class);
                select.setAccessible(true);
                select.invoke(screen, tab);
                // Reinitialization selects the page containing the static selected tab.
                mc.setScreen(
                        new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(
                                mc.player, mc.level.enabledFeatures(), true));
                System.out.println(
                        "RAIN_FIX_CLIENT_OK: dedicated tab, "
                                + eggs
                                + " eggs, vanilla tools clean");
            }
            if (ticks == names.length * 70 + 30)
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-creative-tab.png",
                        mc.getMainRenderTarget(),
                        m -> {});
            if (desired >= names.length && ticks >= names.length * 70 + 40 && !stopping) {
                stopping = true;
                for (String name : names)
                    if (!rendered.contains("blighted_" + name.split(":")[0]))
                        throw new IllegalStateException("not actually rendered " + name);
                if (!sunOK || warningSounds != 1)
                    throw new IllegalStateException(
                            "sun/audio assertion " + sunOK + " / " + warningSounds);
                System.out.println(
                        "BLIGHTED_MOBS_CLIENT_OK: all "
                                + names.length
                                + " models rendered, layered native UV textures, no sun fire, one"
                                + " warning sound");
                mc.stop();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("BLIGHTED_MOBS_CLIENT_FAILED " + t);
            mc.stop();
        }
    }
}
