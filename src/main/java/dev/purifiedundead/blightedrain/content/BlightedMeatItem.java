package dev.purifiedundead.blightedrain.content;

import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class BlightedMeatItem extends Item {
    public BlightedMeatItem() {
        super(
                new Properties()
                        .food(
                                new net.minecraft.world.food.FoodProperties.Builder()
                                        .nutrition(4)
                                        .saturationMod(.1F)
                                        .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        ItemStack result = super.finishUsingItem(stack, level, eater);
        if (!level.isClientSide) {
            eater.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 0));
            eater.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        }
        return result;
    }
}
