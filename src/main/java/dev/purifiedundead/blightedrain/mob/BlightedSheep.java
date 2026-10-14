package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public final class BlightedSheep extends Sheep implements BlightedVariant {
    public BlightedSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getDefaultLootTable() {
        return isSheared()
                ? net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        "minecraft", "entities/sheep")
                : super.getDefaultLootTable();
    }

    @Override
    public net.minecraft.world.entity.MobType getMobType() {
        return net.minecraft.world.entity.MobType.UNDEAD;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
}
