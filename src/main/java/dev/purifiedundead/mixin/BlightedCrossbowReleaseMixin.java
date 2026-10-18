package dev.purifiedundead.mixin;

@org.spongepowered.asm.mixin.Mixin(net.minecraft.world.entity.LivingEntity.class)
public abstract class BlightedCrossbowReleaseMixin {
    @org.spongepowered.asm.mixin.injection.ModifyArg(
            method = "releaseUsingItem",
            at =
                    @org.spongepowered.asm.mixin.injection.At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"),
            index = 2)
    private int remaining(int original) {
        var entity = (net.minecraft.world.entity.LivingEntity) (Object) this;
        var stack = entity.getUseItem();
        if (!dev.purifiedundead.blightedrain.mob.BlightedMobRules.faster(entity)
                || !(stack.getItem() instanceof net.minecraft.world.item.CrossbowItem))
            return original;
        int duration = stack.getUseDuration();
        return Math.max(0, duration - (duration - original) * 2);
    }
}
