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
    public static void register(net.minecraftforge.client.event.RegisterShadersEvent event) {
        precipitation = null;
        ShaderInstance candidate = null;
        try {
            candidate =
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(
                                    "purified_undead", "blighted_precipitation"),
                            DefaultVertexFormat.PARTICLE);
            event.registerShader(candidate, value -> precipitation = value);
        } catch (java.io.IOException | RuntimeException | LinkageError failure) {
            if (candidate != null) {
                ShaderInstance rejected = candidate;
                dev.purifiedundead.util.FailureIsolation.run(
                        "weather shader cleanup", rejected::close);
            }
            dev.purifiedundead.util.FailureIsolation.report(
                    "weather shader; using vanilla weather", failure);
        }
    }

    public static java.util.function.Supplier<ShaderInstance> weather(
            java.util.function.Supplier<ShaderInstance> vanilla) {
        float intensity = BlightedRainClient.intensity();
        if (intensity <= 0 || precipitation == null) return vanilla;
        ShaderInstance shader = precipitation;
        try {
            shader.safeGetUniform("BlightBlend").set(intensity);
            return () -> shader;
        } catch (RuntimeException | LinkageError failure) {
            precipitation = null;
            dev.purifiedundead.util.FailureIsolation.report(
                    "weather shader uniform; using vanilla weather", failure);
            return vanilla;
        }
    }
}
