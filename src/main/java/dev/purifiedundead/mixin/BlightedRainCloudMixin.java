package dev.purifiedundead.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Additive cloud tint. Keep the original cloud calculation and inactive appearance. */
@Mixin(ClientLevel.class)
public abstract class BlightedRainCloudMixin {
    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void purifiedUndead$cloud(float partial, CallbackInfoReturnable<Vec3> result) {
        var color = result.getReturnValue();
        if (dev.purifiedundead.client.BlightedRainClient.intensity() > 0)
            result.setReturnValue(
                    new Vec3(
                            dev.purifiedundead.client.BlightedRainClient.tint((float) color.x, 0),
                            dev.purifiedundead.client.BlightedRainClient.tint((float) color.y, 1),
                            dev.purifiedundead.client.BlightedRainClient.tint((float) color.z, 2)));
    }
}
