package dev.purifiedundead.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;

/** Evoker variant: vanilla summoning and targeting, faster fangs only. */
public final class BlightedKingEntity extends Evoker {
    public static final double MAX_HEALTH = 100;
    public static final int FANG_INTERVAL = 50;

    public BlightedKingEntity(EntityType<? extends Evoker> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Vanilla 1.20.1 puts its fang spell at priority 5, summon at 4 and wololo at 6.
        // Select structurally, without relying on obfuscated class names or reflection.
        var fangs =
                goalSelector.getAvailableGoals().stream()
                        .filter(
                                goal ->
                                        goal.getPriority() == 5
                                                && goal.getGoal()
                                                        instanceof SpellcasterUseSpellGoal)
                        .map(goal -> goal.getGoal())
                        .toList();
        fangs.forEach(goalSelector::removeGoal);
        goalSelector.addGoal(5, new FasterFangsGoal());
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "entities/evoker");
    }

    private final class FasterFangsGoal extends SpellcasterUseSpellGoal {
        @Override
        protected int getCastingTime() {
            return 40;
        }

        @Override
        protected int getCastingInterval() {
            return FANG_INTERVAL;
        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_ATTACK;
        }

        @Override
        protected IllagerSpell getSpell() {
            return IllagerSpell.FANGS;
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity target = getTarget();
            if (target == null || !target.isAlive()) return;
            double low = Math.min(target.getY(), getY());
            double high = Math.max(target.getY(), getY()) + 1;
            float angle = (float) Math.atan2(target.getZ() - getZ(), target.getX() - getX());
            if (distanceToSqr(target) < 9) {
                ring(5, 1.5, angle, 0, low, high);
                ring(8, 2.5, angle + (float) (Math.PI * 0.4), 3, low, high);
            } else {
                for (int i = 0; i < 16; i++) {
                    double distance = 1.25 * (i + 1);
                    spawnFang(
                            getX() + Math.cos(angle) * distance,
                            getZ() + Math.sin(angle) * distance,
                            low,
                            high,
                            angle,
                            i);
                }
            }
        }

        private void ring(
                int count, double radius, float angle, int delay, double low, double high) {
            for (int i = 0; i < count; i++) {
                float heading = angle + (float) (2 * Math.PI * i / count);
                spawnFang(
                        getX() + Math.cos(heading) * radius,
                        getZ() + Math.sin(heading) * radius,
                        low,
                        high,
                        heading,
                        delay);
            }
        }

        private void spawnFang(
                double x, double z, double low, double high, float heading, int delay) {
            BlockPos pos = BlockPos.containing(x, high, z);
            int minimum = Math.max(level().getMinBuildHeight(), (int) Math.floor(low) - 1);
            while (pos.getY() >= minimum) {
                BlockPos below = pos.below();
                if (level().getBlockState(below).isFaceSturdy(level(), below, Direction.UP)) {
                    var shape = level().getBlockState(pos).getCollisionShape(level(), pos);
                    double offset = shape.isEmpty() ? 0 : shape.max(Direction.Axis.Y);
                    level().addFreshEntity(
                                    new EvokerFangs(
                                            level(),
                                            x,
                                            pos.getY() + offset,
                                            z,
                                            heading,
                                            delay,
                                            BlightedKingEntity.this));
                    return;
                }
                pos = below;
            }
        }
    }
}
