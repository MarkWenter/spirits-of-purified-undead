package dev.purifiedundead.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal.class)
public abstract class BlightedCrossbowGoalMixin {
    @org.spongepowered.asm.mixin.gen.Accessor("mob")
    public abstract net.minecraft.world.entity.monster.Monster purifiedUndead$mob();

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 20))
    private int interval(int original) {
        return dev.purifiedundead.blightedrain.mob.BlightedMobRules.interval(
                original, purifiedUndead$mob());
    }

    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/monster/Monster;getTicksUsingItem()I"))
    private int draw(int original) {
        return dev.purifiedundead.blightedrain.mob.BlightedMobRules.faster(purifiedUndead$mob())
                ? original * 2
                : original;
    }
}
