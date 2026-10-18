package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

public final class BlightedPig extends Pig implements BlightedVariant {
    public BlightedPig(EntityType<? extends Pig> type, Level level) {
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
                "purified_undead", "entities/blighted_pig");
    }
}
