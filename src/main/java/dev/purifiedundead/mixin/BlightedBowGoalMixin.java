package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.world.entity.ai.goal.RangedBowAttackGoal.class)
public abstract class BlightedBowGoalMixin {
    @org.spongepowered.asm.mixin.gen.Accessor("mob")
    public abstract net.minecraft.world.entity.Mob purifiedUndead$mob();

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
            method = "tick",
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lnet/minecraft/world/entity/ai/goal/RangedBowAttackGoal;attackIntervalMin:I"))
    private int interval(int original) {
        return dev.purifiedundead.blightedrain.mob.BlightedMobRules.interval(
                original, purifiedUndead$mob());
    }

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/Mob;getTicksUsingItem()I"))
    private int draw(int original) {
        return dev.purifiedundead.blightedrain.mob.BlightedMobRules.faster(purifiedUndead$mob())
                ? original * 2
                : original;
    }
}
