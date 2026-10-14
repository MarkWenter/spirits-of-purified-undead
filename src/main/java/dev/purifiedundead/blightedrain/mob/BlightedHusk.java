package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedHusk extends Husk implements BlightedVariant {
    public BlightedHusk(EntityType<? extends Husk> type, Level level) {
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
                "minecraft", "entities/husk");
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
