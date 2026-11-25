package dev.purifiedundead.mixin;

import dev.purifiedundead.combat.GuardianWaveCombat;
import dev.purifiedundead.content.ModItems;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Some Tetra ports supply no moveset. Keep pack-defined moves and let a socketed melee weapon borrow the Guardian's. */
@Pseudo
@Mixin(targets = "net.bettercombat.logic.WeaponRegistry", remap = false, priority = 900)
abstract class BetterCombatSocketMovesetMixin {
    @Inject(method = "getAttributes(Lnet/minecraft/world/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;",
            at = @At("RETURN"), cancellable = true, require = 0)
    private static void purifiedUndead$socketMoveset(ItemStack stack, CallbackInfoReturnable<WeaponAttributes> result) {
        if (result.getReturnValue() == null && GuardianWaveCombat.socketedMelee(stack)) {
            // The Guardian is a vanilla-style SwordItem and can never take the socket fallback itself.
            result.setReturnValue(WeaponRegistry.getAttributes(ModItems.BLIGHTED_GUARDIAN.get().getDefaultInstance()));
        }
    }
}
