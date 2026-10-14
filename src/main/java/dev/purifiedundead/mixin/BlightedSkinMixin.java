package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.client.renderer.entity.LivingEntityRenderer.class)
public abstract class BlightedSkinMixin {
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
            method = "getRenderType",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getTextureLocation(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/resources/ResourceLocation;"))
    private net.minecraft.resources.ResourceLocation skin(
            net.minecraft.resources.ResourceLocation original,
            net.minecraft.world.entity.LivingEntity entity,
            boolean visible,
            boolean translucent,
            boolean glowing) {
        return dev.purifiedundead.client.BlightedSkins.texture(entity, original);
    }
}
