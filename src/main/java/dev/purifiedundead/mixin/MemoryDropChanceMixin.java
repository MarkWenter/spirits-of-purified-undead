package dev.purifiedundead.mixin;

import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.*;

/** Scale the single random draw, preserving the original threshold including enchantment bonuses. */
@Mixin({LootItemRandomChanceCondition.class, LootItemRandomChanceWithLootingCondition.class})
public abstract class MemoryDropChanceMixin {
    @WrapOperation(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float purifiedUndead$drop(
            RandomSource random, Operation<Float> original, LootContext context) {
        float roll = original.call(random);
        return dev.purifiedundead.slate.MemoryDropChance.applies(context) ? roll * .5F : roll;
    }
}
