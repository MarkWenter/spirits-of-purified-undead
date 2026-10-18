package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.level.Level;

public final class BlightedSheep extends Sheep implements BlightedVariant {
    public BlightedSheep(EntityType<? extends Sheep> type, Level level) {
        super(type, level);
    }

    @Override
    public void shear(net.minecraft.sounds.SoundSource source) {
        var color = getColor();
        try {
            setColor(net.minecraft.world.item.DyeColor.RED);
            super.shear(source);
        } finally {
            setColor(color);
        }
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> onSheared(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.item.ItemStack tool,
            Level world,
            net.minecraft.core.BlockPos pos,
            int fortune) {
        var drops = super.onSheared(player, tool, world, pos, fortune);
        return drops.stream()
                .map(
                        s ->
                                new net.minecraft.world.item.ItemStack(
                                        net.minecraft.world.item.Items.RED_WOOL, s.getCount()))
                .toList();
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
