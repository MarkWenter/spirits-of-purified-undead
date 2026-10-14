package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.world.entity.ai.goal.MeleeAttackGoal.class)
public abstract class BlightedMeleeGoalMixin {
    @org.spongepowered.asm.mixin.gen.Accessor("mob")
    public abstract net.minecraft.world.entity.PathfinderMob purifiedUndead$mob();

    @Inject(method = "getAttackInterval", at = @At("RETURN"), cancellable = true)
    private void interval(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> result) {
        result.setReturnValue(
                dev.purifiedundead.blightedrain.mob.BlightedMobRules.interval(
                        result.getReturnValue(), purifiedUndead$mob()));
    }
}
