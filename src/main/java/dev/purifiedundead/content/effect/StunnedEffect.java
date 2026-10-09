package dev.purifiedundead.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Movement is suppressed by attributes; attack attempts are cancelled by GrothCombatEvents. */
public final class StunnedEffect extends MobEffect {
    public StunnedEffect() {
        super(MobEffectCategory.HARMFUL, 0x66527A);
        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "430EF5AA-997A-4F52-A46E-C66B78E9D21D",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(
                Attributes.ATTACK_SPEED,
                "C2333AFB-57F4-4630-A60A-2DB1BBCE8991",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
