package dev.purifiedundead.mixin;

import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterDropParticle.class)
public abstract class BlightedRainParticleMixin extends TextureSheetParticle {
    protected BlightedRainParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void purifiedUndead$rainParticle(
            ClientLevel level, double x, double y, double z, CallbackInfo ci) {
        float f = dev.purifiedundead.client.BlightedRainClient.intensity();
        if (f <= 0
                || !dev.purifiedundead.blightedrain.RainExposure.precipitation(
                        level, net.minecraft.core.BlockPos.containing(x, y, z))) return;
        setColor(rCol * (1 - .35F * f), gCol * (1 - .52F * f), bCol * (1 - .53F * f));
    }
}
