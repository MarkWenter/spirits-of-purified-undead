package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;

public final class BlightedCow extends Cow implements BlightedVariant {
    public BlightedCow(EntityType<? extends Cow> type, Level level) {
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
                "purified_undead", "entities/blighted_cow");
    }
}
