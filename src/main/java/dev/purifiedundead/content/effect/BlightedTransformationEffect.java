package dev.purifiedundead.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Ten-minute ritual state whose next death grants the Ancient Contract after respawn. */
public final class BlightedTransformationEffect extends MobEffect {
    public BlightedTransformationEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A183F);
    }
}
