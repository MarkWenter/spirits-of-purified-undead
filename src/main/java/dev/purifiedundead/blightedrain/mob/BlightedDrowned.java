package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.Level;

public final class BlightedDrowned extends Drowned implements BlightedVariant {
    public BlightedDrowned(EntityType<? extends Drowned> type, Level level) {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag data) {
        super.addAdditionalSaveData(data);
        data.putInt("BlightedTraitsVersion", 1);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag data) {
        super.readAdditionalSaveData(data);
        BlightedMobRules.readLegacy(this, data);
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
                "minecraft", "entities/drowned");
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
