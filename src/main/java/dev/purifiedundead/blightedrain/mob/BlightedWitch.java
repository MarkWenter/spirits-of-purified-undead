package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedWitch extends Witch implements BlightedVariant {
    public BlightedWitch(EntityType<? extends Witch> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        adaptHealingPotion();
        super.aiStep();
        adaptHealingPotion();
    }

    /** Healing hurts undead; adapt both newly selected drinks and loaded/in-progress drinks. */
    private void adaptHealingPotion() {
        if (!level().isClientSide
                && isDrinkingPotion()
                && net.minecraft.world.item.alchemy.PotionUtils.getPotion(getMainHandItem())
                        == net.minecraft.world.item.alchemy.Potions.HEALING) {
            net.minecraft.world.item.alchemy.PotionUtils.setPotion(
                    getMainHandItem(), net.minecraft.world.item.alchemy.Potions.HARMING);
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
                "minecraft", "entities/witch");
    }
}
