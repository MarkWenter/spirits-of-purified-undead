package dev.purifiedundead.client;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.content.ModEntities;
import dev.purifiedundead.content.ModParticles;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@Mod.EventBusSubscriber(modid = PurifiedUndead.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(WispDynamicLights::install);
        event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(dev.purifiedundead.foundry.FoundryContent.MENU.get(), PurificationFoundryScreen::new));
        event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(dev.purifiedundead.slate.SlateContent.MENU.get(), LilyMemoryScreen::new));
    }
    @SubscribeEvent
    public static void potionColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer == 0
                ? (dev.purifiedundead.progress.PureElixirBrewing.isPure(stack)
                    ? (0xFF000000 | dev.purifiedundead.content.ModPotions.PURE_ELIXIR_COLOR) : net.minecraft.world.item.alchemy.PotionUtils.getColor(stack))
                : -1, net.minecraft.world.item.Items.POTION, net.minecraft.world.item.Items.SPLASH_POTION,
                net.minecraft.world.item.Items.LINGERING_POTION);
    }
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CONTRACT_WISP.get(), ContractWispRenderer::new);
        event.registerEntityRenderer(ModEntities.FERIN.get(), FerinRenderer::new);
        event.registerEntityRenderer(ModEntities.GUARDIAN_WAVE.get(), GuardianWaveRenderer::new);
        event.registerEntityRenderer(ModEntities.BLIGHTED_GOLEM.get(), BlightedGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.BLIGHTED_KING.get(), BlightedKingRenderer::new);
        event.registerEntityRenderer(ModEntities.ELEINE_MAGIC_ORB.get(), EleineMagicOrbRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FERIN_SLASH.get(), FerinSlashParticle.Provider::new);
        event.registerSpriteSet(ModParticles.HOENIR_MARK.get(), HoenirMarkParticle.Provider::new);
    }
}
