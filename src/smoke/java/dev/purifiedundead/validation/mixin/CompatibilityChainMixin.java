package dev.purifiedundead.validation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** A second independent wrapper, used only in opt-in developer regressions. */
@Mixin(value = LootItemRandomChanceCondition.class, priority = 900)
public abstract class CompatibilityChainMixin {
    @WrapOperation(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float auditChain(RandomSource random, Operation<Float> original) {
        dev.purifiedundead.validation.CompatibilitySmoke.chainCalls++;
        return original.call(random);
    }
}
