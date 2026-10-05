package dev.purifiedundead.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

/** Vanilla iron golem behavior with a ten-tick melee interval instead of twenty. */
public final class BlightedGolemEntity extends IronGolem {
    public static final double MAX_HEALTH = 200;
    public BlightedGolemEntity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getInt("purified_undead:blighted_stats_version") < 1) {
            float previousMaximum = getMaxHealth(), previousHealth = getHealth();
            getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(MAX_HEALTH);
            setHealth(BlightedHealth.rescale(previousHealth, previousMaximum, getMaxHealth()));
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("purified_undead:blighted_stats_version", 1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeAllGoals(goal -> goal instanceof MeleeAttackGoal);
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true) {
            @Override
            protected int getAttackInterval() {
                return 10;
            }
        });
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "entities/iron_golem");
    }
}
