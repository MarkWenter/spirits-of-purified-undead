package dev.purifiedundead.blightedrain.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.level.Level;

public final class BlightedVillager extends ZombieVillager implements BlightedVariant {
    public BlightedVillager(EntityType<? extends ZombieVillager> type, Level level) {
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
    public net.minecraft.world.InteractionResult mobInteract(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (stack.is(net.minecraft.world.item.Items.POTION)
                && dev.purifiedundead.progress.PureElixirBrewing.isPure(stack)) {
            if (!isConverting() && !level().isClientSide) {
                // Vanilla's save-data entry starts its own cure timer, effects, sound and player
                // credit.
                var data = new net.minecraft.nbt.CompoundTag();
                addAdditionalSaveData(data);
                data.putInt("ConversionTime", 3600 + getRandom().nextInt(2401));
                data.putUUID("ConversionPlayer", player.getUUID());
                readAdditionalSaveData(data);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    var bottle =
                            new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.GLASS_BOTTLE);
                    if (stack.isEmpty()) player.setItemInHand(hand, bottle);
                    else if (!player.getInventory().add(bottle)) player.drop(bottle, false);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);
        }
        // The elixir is the sole cure entry for this variant, not weakness plus golden apple.
        if (stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE))
            return net.minecraft.world.InteractionResult.PASS;
        return super.mobInteract(player, hand);
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
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }
}
