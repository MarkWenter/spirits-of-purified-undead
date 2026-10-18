package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedDonkey extends Donkey implements BlightedVariant {
    public BlightedDonkey(EntityType<? extends Donkey> type, Level level) {
        super(type, level);
    }

    @Override
    public net.minecraft.world.entity.MobType getMobType() {
        return net.minecraft.world.entity.MobType.UNDEAD;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected net.minecraft.resources.ResourceLocation getDefaultLootTable() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "minecraft", "entities/donkey");
    }

    @Override
    protected void randomizeAttributes(net.minecraft.util.RandomSource random) {
        super.randomizeAttributes(random);
        var health = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        health.setBaseValue(health.getBaseValue() * 2);
    }
}
