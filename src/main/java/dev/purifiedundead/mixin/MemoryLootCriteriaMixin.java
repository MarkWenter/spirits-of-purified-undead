package dev.purifiedundead.mixin;

import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(DamageSourceCondition.class)
public abstract class MemoryLootCriteriaMixin {
    @WrapOperation(
            method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/advancements/critereon/DamageSourcePredicate;matches(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean purifiedUndead$loot(
            DamageSourcePredicate predicate,
            ServerLevel level,
            Vec3 position,
            DamageSource source,
            Operation<Boolean> original) {
        return dev.purifiedundead.slate.MemoryDamageCriteria.evaluate(
                source, () -> original.call(predicate, level, position, source));
    }
}
