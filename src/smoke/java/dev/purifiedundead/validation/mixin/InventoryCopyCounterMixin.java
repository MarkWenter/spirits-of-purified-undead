package dev.purifiedundead.validation.mixin;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.item.ItemStack.class)
public abstract class InventoryCopyCounterMixin {
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "copy",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void purifiedUndead$countCopy(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<
                            net.minecraft.world.item.ItemStack>
                    ci) {
        if (dev.purifiedundead.progress.FailureBoundarySmoke.countCopies)
            dev.purifiedundead.progress.FailureBoundarySmoke.itemCopies++;
    }
}
