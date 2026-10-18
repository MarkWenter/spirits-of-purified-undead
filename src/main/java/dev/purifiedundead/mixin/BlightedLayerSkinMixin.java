package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.client.renderer.entity.layers.RenderLayer.class)
public abstract class BlightedLayerSkinMixin {
    @ModifyVariable(method = "coloredCutoutModelCopyLayerRender", at = @At("HEAD"), argsOnly = true)
    private static net.minecraft.resources.ResourceLocation skin(
            net.minecraft.resources.ResourceLocation original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true)
                    net.minecraft.world.entity.LivingEntity entity) {
        return dev.purifiedundead.client.BlightedSkins.texture(entity, original);
    }
}
