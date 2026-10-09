package dev.purifiedundead.mixin;

import net.minecraft.world.level.storage.loot.LootContext;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = net.minecraftforge.common.loot.LootModifier.class, remap = false)
public abstract class CelestialEtchingLootMixin {
    @WrapOperation(
            method = "apply",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"),
            remap = false)
    private boolean purifiedUndead$etching(
            Predicate<LootContext> predicate, Object context, Operation<Boolean> original) {
        return dev.purifiedundead.compat.CelestialEtchingCompatibility.test(
                this, value -> original.call(predicate, value), (LootContext) context);
    }
}
