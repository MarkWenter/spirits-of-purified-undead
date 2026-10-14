package dev.purifiedundead.mixin;

import net.minecraft.client.particle.WaterDropParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterDropParticle.class)
public abstract class BlightedRainParticleMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void purifiedUndead$rainParticle(CallbackInfo ci) {
        float f = dev.purifiedundead.client.BlightedRainClient.intensity();
        ((net.minecraft.client.particle.Particle) (Object) this)
                .setColor(1 - f + .65F * f, 1 - f + .48F * f, 1 - f + .47F * f);
    }
}
