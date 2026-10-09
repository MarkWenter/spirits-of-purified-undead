package dev.purifiedundead.mixin;

import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.*;

/** Criteria only: the real combat DamageSource and all mitigation remain untouched. */
@Mixin(DamageSourcePredicate.class)
public abstract class MemoryDamageCriteriaMixin {
    @WrapOperation(
            method =
                    "matches(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/damagesource/DamageSource;)Z",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/advancements/critereon/TagPredicate;matches(Lnet/minecraft/core/Holder;)Z"))
    private boolean purifiedUndead$memory(
            TagPredicate<DamageType> predicate,
            Holder<DamageType> holder,
            Operation<Boolean> original,
            ServerLevel level,
            Vec3 origin,
            DamageSource source) {
        boolean nativeResult = original.call(predicate, holder);
        if (!dev.purifiedundead.slate.MemoryDamageCriteria.active(source)
                || !(source.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)
                || !dev.purifiedundead.slate.MemoryStorage.active(p, "hoenir")) return nativeResult;
        var accessor = (MemoryTagPredicateAccessor) (Object) predicate;
        var tag = accessor.purifiedUndead$tag();
        boolean expected = accessor.purifiedUndead$expected();
        var damage = p.damageSources();
        boolean matches =
                (expected == nativeResult)
                        || damage.wither().is(tag)
                        || damage.magic().is(tag)
                        || damage.onFire().is(tag)
                        || damage.freeze().is(tag);
        if (tag.equals(net.minecraft.tags.DamageTypeTags.BYPASSES_ENCHANTMENTS)
                && dev.purifiedundead.compat.CelestialEtchingCompatibility.active(source))
            matches = true;
        return matches == expected;
    }
}
