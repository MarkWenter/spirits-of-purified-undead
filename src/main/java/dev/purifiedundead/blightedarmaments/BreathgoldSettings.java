package dev.purifiedundead.blightedarmaments;

/** Registry-time settings. A single immutable snapshot is shared by tools and ore tracking. */
public final class BreathgoldSettings {
    private static final ArmamentProperties CONFIG = new ArmamentProperties("purified_undead-breathgold.properties");
    public static final int CONVERSION_TICKS = CONFIG.integer("conversionTicks", 200, 1, 1200000);
    static final int MATERIAL_STACK = CONFIG.integer("materialStackSize", 64, 1, 64);
    static final int ORE_STACK = CONFIG.integer("oreStackSize", 64, 1, 64);
    static final int DURABILITY = CONFIG.integer("toolDurability", 375, 1, 1000000);
    static final float MINING_SPEED = CONFIG.decimal("miningSpeed", 6, 0, 1024);
    static final int ENCHANTABILITY = CONFIG.integer("toolEnchantability", legacyToolEnchantability(), 0, 255);
    static final float TIER_DAMAGE = CONFIG.decimal("tierDamageBonus", 2, 0, 1024);

    private static int legacyToolEnchantability() {
        int old = CONFIG.integer("enchantability", 14, 0, 255);
        return old == 14 ? 22 : old;
    }

    static final int ARMOR_ENCHANTABILITY = CONFIG.integer("armorEnchantability", 25, 0, 255);
    static final int INGOT_STACK = CONFIG.integer("ingotStackSize", 64, 1, 64);

    static final int HELMET_DURABILITY = CONFIG.integer("helmet.durability", 248, 1, 1000000);
    static final int HELMET_DEFENSE = CONFIG.integer("helmet.defense", 2, 0, 30);

    static final int CHESTPLATE_DURABILITY = CONFIG.integer("chestplate.durability", 360, 1, 1000000);
    static final int CHESTPLATE_DEFENSE = CONFIG.integer("chestplate.defense", 6, 0, 30);

    static final int LEGGINGS_DURABILITY = CONFIG.integer("leggings.durability", 338, 1, 1000000);
    static final int LEGGINGS_DEFENSE = CONFIG.integer("leggings.defense", 5, 0, 30);

    static final int BOOTS_DURABILITY = CONFIG.integer("boots.durability", 293, 1, 1000000);
    static final int BOOTS_DEFENSE = CONFIG.integer("boots.defense", 2, 0, 30);

    private BreathgoldSettings() {}

    static int damage(String tool, int fallback) {
        return CONFIG.integer(tool + ".damage", fallback, -2, 1024);
    }

    static float attackSpeed(String tool, float fallback) {
        return CONFIG.decimal(tool + ".attackSpeed", fallback, -3.99F, 16);
    }

    static float shovelDamage() {
        return CONFIG.decimal("shovel.damage", 1.5F, -2, 1024);
    }
}
