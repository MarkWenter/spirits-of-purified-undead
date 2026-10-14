package dev.purifiedundead.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve vanilla rain/snow geometry and every other wrapper, tinting only weather vertices. */
@Mixin(LevelRenderer.class)
public abstract class BlightedRainVisualMixin {
    @org.spongepowered.asm.mixin.Unique private float purifiedUndead$weatherTint;
    @org.spongepowered.asm.mixin.Unique private int purifiedUndead$weatherVertex;

    @org.spongepowered.asm.mixin.injection.Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void purifiedUndead$begin(
            net.minecraft.client.renderer.LightTexture light,
            float partial,
            double x,
            double y,
            double z,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        purifiedUndead$weatherTint = dev.purifiedundead.client.BlightedRainClient.intensity();
        purifiedUndead$weatherVertex = 0;
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/systems/RenderSystem;setShader(Ljava/util/function/Supplier;)V",
                            remap = false))
    private void purifiedUndead$weatherShader(
            java.util.function.Supplier<net.minecraft.client.renderer.ShaderInstance> vanilla,
            Operation<Void> original) {
        original.call(dev.purifiedundead.client.BlightedRainShaders.weather(vanilla));
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer purifiedUndead$rainColor(
            VertexConsumer vertex,
            float red,
            float green,
            float blue,
            float alpha,
            Operation<VertexConsumer> original) {
        float f = purifiedUndead$weatherTint;
        boolean stain = (purifiedUndead$weatherVertex++ / 4) % 23 == 0;
        return original.call(
                vertex,
                red * (1 - f + (stain ? .78F : .62F) * f),
                green * (1 - f + (stain ? .22F : .62F) * f),
                blue * (1 - f + (stain ? .20F : .62F) * f),
                alpha);
    }
}
