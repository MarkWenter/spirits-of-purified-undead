package dev.purifiedundead.compat;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;

/** Optional death-time qualification; Malum alone handles drops, bonuses and soulless/spawner restrictions. */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "purified_undead")
public final class GuardianMalumSoulCompatibility {
    private interface Exposer {
        void expose(LivingEntity victim) throws ReflectiveOperationException;
    }

    private static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> NATIVE_TAG =
            net.minecraft.tags.TagKey.create(
                    net.minecraft.core.registries.Registries.ITEM,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "malum", "soul_hunter_weapon"));
    private static Exposer bridge;
    private static boolean checked;

    @net.minecraftforge.eventbus.api.SubscribeEvent(
            priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void onDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        var victim = event.getEntity();
        if (victim.level().isClientSide
                || victim instanceof net.minecraft.world.entity.player.Player
                || event.isCanceled()
                || !PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.guardianMalumSoulHarvest)
                || !(event.getSource().getEntity() instanceof ServerPlayer owner)
                || !owner.getMainHandItem().is(ModItems.BLIGHTED_GUARDIAN.get())) return;
        if (owner.getMainHandItem().is(NATIVE_TAG)) return;
        if (!checked) initialize();
        if (bridge == null) return;
        try {
            bridge.expose(victim);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
            disable(failure);
        }
    }

    private static void initialize() {
        checked = true;
        if (!net.minecraftforge.fml.ModList.get().isLoaded("malum")) return;
        try {
            var expose =
                    Class.forName("com.sammy.malum.core.handlers.SoulDataHandler")
                            .getMethod("exposeSoul", LivingEntity.class);
            bridge = victim -> expose.invoke(null, victim);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
            disable(failure);
        }
    }

    private static void disable(Throwable failure) {
        bridge = null;
        com.mojang.logging.LogUtils.getLogger()
                .warn(
                        "Optional Malum guardian soul bridge disabled due to incompatible API; normal combat remains active",
                        failure);
    }

    private GuardianMalumSoulCompatibility() {}
}
