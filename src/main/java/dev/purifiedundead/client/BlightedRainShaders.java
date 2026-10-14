package dev.purifiedundead.client;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = "purified_undead",
        bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class BlightedRainShaders {
    private static ShaderInstance precipitation;

    private BlightedRainShaders() {}

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void register(net.minecraftforge.client.event.RegisterShadersEvent event)
            throws java.io.IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(
                                "purified_undead", "blighted_precipitation"),
                        DefaultVertexFormat.PARTICLE),
                value -> precipitation = value);
    }

    public static java.util.function.Supplier<ShaderInstance> weather(
            java.util.function.Supplier<ShaderInstance> vanilla) {
        float intensity = BlightedRainClient.intensity();
        if (intensity <= 0 || precipitation == null) return vanilla;
        precipitation.safeGetUniform("BlightBlend").set(intensity);
        return () -> precipitation;
    }
}
