package dev.purifiedundead.purification;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.sounds.*;
import dev.purifiedundead.content.ModItems;

public final class ImmaculateArmor extends ArmorItem {
    private static final ArmorMaterial MATERIAL =
            new ArmorMaterial() {
                public int getDurabilityForType(Type t) {
                    return durability(t);
                }

                public int getDefenseForType(Type t) {
                    return defense(t);
                }

                public int getEnchantmentValue() {
                    return 15;
                }

                public SoundEvent getEquipSound() {
                    return SoundEvents.ARMOR_EQUIP_NETHERITE;
                }

                public Ingredient getRepairIngredient() {
                    return Ingredient.of(ModItems.PURIFIED_ARCSTEEL.get());
                }

                public String getName() {
                    return "purified_undead:immaculate";
                }

                public float getToughness() {
                    return 3;
                }

                public float getKnockbackResistance() {
                    return 1;
                }
            };

    public ImmaculateArmor(Type t) {
        super(MATERIAL, t, new Item.Properties().fireResistant().rarity(Rarity.EPIC));
    }

    public static int durability(Type t) {
        return switch (t) {
            case HELMET -> 667;
            case CHESTPLATE -> 898;
            case LEGGINGS -> 847;
            case BOOTS -> 738;
            default -> 0;
        };
    }

    public static int defense(Type t) {
        return switch (t) {
            case HELMET -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
            default -> 0;
        };
    }

    @Override
    public void appendHoverText(
            ItemStack s,
            net.minecraft.world.level.Level level,
            java.util.List<net.minecraft.network.chat.Component> lines,
            TooltipFlag flag) {
        lines.add(
                net.minecraft.network.chat.Component.translatable(getDescriptionId() + ".desc")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(
                net.minecraft.network.chat.Component.translatable(
                                "tooltip.purified_undead.immaculate")
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
