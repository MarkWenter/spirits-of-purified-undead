package dev.purifiedundead.content.relic;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/** Shared extension contract and configurable scaling for White Witch Relics. */
public interface WhiteWitchRelicEffect {
    ResourceLocation relicId();

    default float modifyIncomingDamage(ServerPlayer wearer, DamageSource source, float amount) {
        return amount;
    }

    default float modifyOutgoingDamage(ServerPlayer wearer, DamageSource source, float amount) {
        return amount;
    }

    default void serverTick(ServerPlayer wearer) {
    }

    default double scaleEffect(double baseValue) {
        return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicsEnabled)
                ? baseValue * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale) : 0.0D;
    }

    default int scaleCooldown(int baseTicks) {
        return Math.max(1, (int) Math.round(baseTicks
                * PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicCooldownScale)));
    }
}
