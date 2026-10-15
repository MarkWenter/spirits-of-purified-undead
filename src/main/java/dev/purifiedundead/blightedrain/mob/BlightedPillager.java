package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedPillager extends Pillager implements BlightedVariant {
    public BlightedPillager(EntityType<? extends Pillager> type, Level level) {
        super(type, level);
    }


    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0, false) {
            @Override public boolean canUse() {
                return !isHolding(net.minecraft.world.item.Items.CROSSBOW) && super.canUse();
            }
            @Override public boolean canContinueToUse() {
                return !isHolding(net.minecraft.world.item.Items.CROSSBOW) && super.canContinueToUse();
            }
        });
    }

    @Override
    protected void pickUpItem(net.minecraft.world.entity.item.ItemEntity entity) {
        var stack = entity.getItem();
        if (stack.is(net.minecraft.world.item.Items.CROSSBOW) && getMainHandItem().isEmpty()) {
            onItemPickup(entity);
            setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, stack.split(1));
            setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 2.0F);
            setPersistenceRequired();
            take(entity, 1);
            if (stack.isEmpty()) entity.discard();
        } else {
            super.pickUpItem(entity);
        }
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
                "minecraft", "entities/pillager");
    }
}
