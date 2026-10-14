package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedVillager extends ZombieVillager implements BlightedVariant {
    public BlightedVillager(EntityType<? extends ZombieVillager> type, Level level) {
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
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
