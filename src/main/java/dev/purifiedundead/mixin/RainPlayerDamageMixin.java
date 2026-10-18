package dev.purifiedundead.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.*;

/** Record accepted player damage, including absorption, after the real damage operation. */
@Mixin(LivingEntity.class)
public abstract class RainPlayerDamageMixin {
    @WrapOperation(
            method = "hurt",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void purifiedUndead$playerHit(
            LivingEntity target, DamageSource source, float amount, Operation<Void> original) {
        float health = target.getHealth(), absorption = target.getAbsorptionAmount();
        original.call(target, source, amount);
        if (target.level() instanceof net.minecraft.server.level.ServerLevel
                && source.getEntity() instanceof ServerPlayer
                && (target.getHealth() < health || target.getAbsorptionAmount() < absorption))
            target.getPersistentData()
                    .putBoolean(dev.purifiedundead.blightedrain.RainEffects.PLAYER_HIT, true);
    }
}
