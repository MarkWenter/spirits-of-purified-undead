package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedSkeleton extends Skeleton implements BlightedVariant {
    public BlightedSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }

    @Override
    public net.minecraft.world.entity.MobType getMobType() {
        return net.minecraft.world.entity.MobType.UNDEAD;
    }

    @Override
    protected void doFreezeConversion() {
        this.convertTo(BlightedMobContent.STRAY.get(), true);
        if (!this.isSilent()) this.level().levelEvent(null, 1048, this.blockPosition(), 0);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected net.minecraft.resources.ResourceLocation getDefaultLootTable() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "minecraft", "entities/skeleton");
    }
}
