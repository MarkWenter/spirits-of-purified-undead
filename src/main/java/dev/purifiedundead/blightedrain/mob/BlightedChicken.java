package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedChicken extends Chicken implements BlightedVariant {
    public BlightedChicken(EntityType<? extends Chicken> type, Level level) {
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
                "minecraft", "entities/chicken");
    }
}
