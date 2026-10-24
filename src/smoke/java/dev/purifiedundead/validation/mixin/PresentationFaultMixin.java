package dev.purifiedundead.validation.mixin;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.inventory.AbstractContainerMenu.class)
public abstract class PresentationFaultMixin {
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "broadcastChanges",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void purifiedUndead$failPresentation(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if ((Object) this == dev.purifiedundead.progress.FailureBoundarySmoke.badMenu) {
            dev.purifiedundead.progress.FailureBoundarySmoke.syncFaults++;
            throw new IllegalStateException("expected inventory presentation failure");
        }
    }
}
