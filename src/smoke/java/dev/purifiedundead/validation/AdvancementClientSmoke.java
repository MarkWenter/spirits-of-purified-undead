package dev.purifiedundead.validation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.ResourceLocation;
import dev.purifiedundead.progress.ModAdvancements;
import dev.purifiedundead.content.ModEffects;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class AdvancementClientSmoke {
    private static boolean loading;
    private static int ticks;

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END
                || !Boolean.getBoolean("purified_undead.advancementSmoke")) return;
        var mc = Minecraft.getInstance();
        try {
            if (!loading && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                loading = true;
                mc.createWorldOpenFlows().loadLevel(mc.screen, "diary-validation");
                return;
            }
            if (mc.level == null || mc.player == null || mc.getOverlay() != null) return;
            ticks++;
            if (ticks == 60) {
                for (String id : ModAdvancements.IDS) {
                    var item =
                            net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                                    ResourceLocation.fromNamespaceAndPath(
                                            "purified_undead", "advancement_" + id));
                    var model =
                            mc.getItemRenderer()
                                    .getModel(
                                            new net.minecraft.world.item.ItemStack(item),
                                            mc.level,
                                            mc.player,
                                            0);
                    if (model == mc.getModelManager().getMissingModel()
                            || model.getParticleIcon()
                                    .contents()
                                    .name()
                                    .getPath()
                                    .equals("missingno"))
                        throw new IllegalStateException("icon missing " + id);
                }
                var sprite =
                        mc.getMobEffectTextures().get(ModEffects.BLIGHTED_TRANSFORMATION.get());
                if (sprite.contents().name().getPath().equals("missingno"))
                    throw new IllegalStateException("effect sprite missing");
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    var p =
                                            mc.getSingleplayerServer()
                                                    .getPlayerList()
                                                    .getPlayer(mc.player.getUUID());
                                    for (String id : ModAdvancements.IDS)
                                        ModAdvancements.award(p, id);
                                    p.addEffect(
                                            new net.minecraft.world.effect.MobEffectInstance(
                                                    ModEffects.BLIGHTED_TRANSFORMATION.get(),
                                                    12000));
                                });
            }
            if (ticks == 100) {
                var manager = mc.player.connection.getAdvancements();
                mc.setScreen(new AdvancementsScreen(manager));
                manager.setSelectedTab(
                        manager.getAdvancements()
                                .get(
                                        ResourceLocation.fromNamespaceAndPath(
                                                "purified_undead", "journey/hidden_power")),
                        false);
            }
            if (ticks == 110 || ticks == 135) {
                mc.getToasts().clear();
                mc.gui.getChat().clearMessages(false);
            }
            if (ticks == 120) {
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "advancements-043.png",
                        mc.getMainRenderTarget(),
                        m -> {});
                mc.setScreen(
                        new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
            }
            if (ticks == 145) {
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory,
                        "blighted-effect-043.png",
                        mc.getMainRenderTarget(),
                        m -> {});
                System.out.println(
                        "ADVANCEMENTS_CLIENT_OK: 12 icon models, effect atlas, advancement tree and inventory status rendered");
                mc.stop();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.out.println("ADVANCEMENTS_CLIENT_FAILED");
            mc.stop();
        }
    }
}
