package dev.purifiedundead.blightedarmaments;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.*;

public final class BreathgoldArmor extends ArmorItem {
    private static final ArmorMaterial MATERIAL =
            new ArmorMaterial() {
                public int getDurabilityForType(Type t) {
                    return durability(t);
                }

                public int getDefenseForType(Type t) {
                    return defense(t);
                }

                public int getEnchantmentValue() {
                    return BreathgoldSettings.ARMOR_ENCHANTABILITY;
                }

                public SoundEvent getEquipSound() {
                    return SoundEvents.ARMOR_EQUIP_IRON;
                }

                public Ingredient getRepairIngredient() {
                    return BreathgoldContent.repairMaterial();
                }

                public String getName() {
                    return "purified_undead:breathgold";
                }

                public float getToughness() {
                    return 0;
                }

                public float getKnockbackResistance() {
                    return 0;
                }
            };

    public BreathgoldArmor(Type t) {
        super(MATERIAL, t, new Item.Properties());
    }

    public static int durability(Type t) {
        return switch (t) {
            case HELMET -> BreathgoldSettings.HELMET_DURABILITY;
            case CHESTPLATE -> BreathgoldSettings.CHESTPLATE_DURABILITY;
            case LEGGINGS -> BreathgoldSettings.LEGGINGS_DURABILITY;
            case BOOTS -> BreathgoldSettings.BOOTS_DURABILITY;
            default -> 0;
        };
    }

    public static int defense(Type t) {
        return switch (t) {
            case HELMET -> BreathgoldSettings.HELMET_DEFENSE;
            case CHESTPLATE -> BreathgoldSettings.CHESTPLATE_DEFENSE;
            case LEGGINGS -> BreathgoldSettings.LEGGINGS_DEFENSE;
            case BOOTS -> BreathgoldSettings.BOOTS_DEFENSE;
            default -> 0;
        };
    }
}
