package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.world.entity.ai.goal.RangedAttackGoal.class)
public abstract class BlightedRangedGoalMixin {
    @org.spongepowered.asm.mixin.gen.Accessor("mob")
    public abstract net.minecraft.world.entity.Mob purifiedUndead$mob();

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
            method = "tick",
            at = {
                @At(
                        value = "FIELD",
                        target =
                                "Lnet/minecraft/world/entity/ai/goal/RangedAttackGoal;attackIntervalMin:I"),
                @At(
                        value = "FIELD",
                        target =
                                "Lnet/minecraft/world/entity/ai/goal/RangedAttackGoal;attackIntervalMax:I")
            })
    private int interval(int original) {
        return dev.purifiedundead.blightedrain.mob.BlightedMobRules.interval(
                original, purifiedUndead$mob());
    }
}
