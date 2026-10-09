package dev.purifiedundead.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(KilledTrigger.TriggerInstance.class)
public abstract class MemoryKillCriteriaMixin {
    @WrapOperation(
            method =
                    "matches(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/storage/loot/LootContext;Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/advancements/critereon/DamageSourcePredicate;matches(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean purifiedUndead$kill(
            DamageSourcePredicate predicate,
            ServerPlayer player,
            DamageSource source,
            Operation<Boolean> original) {
        return dev.purifiedundead.slate.MemoryDamageCriteria.evaluate(
                source, () -> original.call(predicate, player, source));
    }
}
