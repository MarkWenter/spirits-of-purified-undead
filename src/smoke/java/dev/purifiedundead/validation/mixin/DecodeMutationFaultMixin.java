package dev.purifiedundead.validation.mixin;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.item.Item.class)
public abstract class DecodeMutationFaultMixin {
    @org.spongepowered.asm.mixin.injection.Inject(
            method = "verifyTagAfterLoad",
            at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void purifiedUndead$mutateBeforeFailure(
            net.minecraft.nbt.CompoundTag tag,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (dev.purifiedundead.progress.HotfixBoundarySmoke.failDecode
                && tag.contains("hotfix_decode_probe")) {
            tag.putString("hotfix_decode_probe", "mutated by failing decoder");
            dev.purifiedundead.progress.HotfixBoundarySmoke.decodeFaults++;
            throw new IllegalStateException("expected mutating item decoder failure");
        }
    }
}
