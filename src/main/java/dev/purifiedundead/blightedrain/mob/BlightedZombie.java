package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedZombie extends Zombie implements BlightedVariant {
    public BlightedZombie(EntityType<? extends Zombie> type, Level level) {
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
                "minecraft", "entities/zombie");
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
