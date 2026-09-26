package dev.purifiedundead.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;

/** Vanilla iron golem behavior with a ten-tick melee interval instead of twenty. */
public final class BlightedGolemEntity extends IronGolem {
    public BlightedGolemEntity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
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
        return ResourceLocation.withDefaultNamespace("entities/iron_golem");
    }
}
