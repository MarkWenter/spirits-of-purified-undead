package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public final class BlightedSpider extends Spider implements BlightedVariant {
    public BlightedSpider(EntityType<? extends Spider> type, Level level) {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BlightedSpiderTraits", 1);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Attributes", 9) && tag.getInt("BlightedSpiderTraits") < 1) {
            float ratio = getHealth() / getMaxHealth();
            var health =
                    getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
            health.setBaseValue(health.getBaseValue() * .75);
            for (var modifier : java.util.List.copyOf(health.getModifiers())) {
                if (modifier.getOperation()
                        == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
                                .ADDITION) {
                    health.removeModifier(modifier);
                    health.addPermanentModifier(
                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                    modifier.getId(),
                                    modifier.getName(),
                                    modifier.getAmount() * .75,
                                    modifier.getOperation()));
                }
            }
            var speed =
                    getAttribute(
                            net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            speed.setBaseValue(speed.getBaseValue() * .8);
            setHealth(ratio * getMaxHealth());
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
                "minecraft", "entities/spider");
    }
}
