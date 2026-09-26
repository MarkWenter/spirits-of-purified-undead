package dev.purifiedundead.content.enchantment;

import dev.purifiedundead.content.item.AncientContractItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** One of the contract's eight independent, non-lootable blight curses. */
public final class BlightCurseEnchantment extends Enchantment {
    private static final EnchantmentCategory CONTRACT = EnchantmentCategory.create(
            "purified_undead_contract", item -> item instanceof AncientContractItem);

    public BlightCurseEnchantment() {
        super(Rarity.VERY_RARE, CONTRACT, EquipmentSlot.values());
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }
}
