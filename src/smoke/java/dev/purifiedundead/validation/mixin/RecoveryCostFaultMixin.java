package dev.purifiedundead.validation.mixin;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.Entity.class)
public abstract class RecoveryCostFaultMixin {
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "discard",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void purifiedUndead$failCleanup(
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if ((Object) this == dev.purifiedundead.progress.FailureBoundarySmoke.badDiscard)
            throw new IllegalStateException("expected cleanup second failure");
    }
}
