package dev.purifiedundead.blightedarmaments;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class BreathgoldContent {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, "purified_undead");
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "purified_undead");
    public static final RegistryObject<Block> ORE =
            BLOCKS.register(
                    "breathgold_ore",
                    () -> new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_ORE)));
    public static final RegistryObject<Item> ORE_ITEM =
            ITEMS.register(
                    "breathgold_ore",
                    () ->
                            new BlockItem(
                                    ORE.get(),
                                    new Item.Properties().stacksTo(BreathgoldSettings.ORE_STACK)));
    public static final RegistryObject<Item> MATERIAL =
            ITEMS.register("breathgold", BreathgoldItem::new);
    public static final RegistryObject<Item> INGOT = ITEMS.register("breathgold_ingot", () -> new Item(new Item.Properties().stacksTo(BreathgoldSettings.INGOT_STACK)));
    public static final RegistryObject<Item> HELMET = ITEMS.register("breathgold_helmet", () -> new BreathgoldArmor(ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> CHESTPLATE = ITEMS.register("breathgold_chestplate", () -> new BreathgoldArmor(ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> LEGGINGS = ITEMS.register("breathgold_leggings", () -> new BreathgoldArmor(ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> BOOTS = ITEMS.register("breathgold_boots", () -> new BreathgoldArmor(ArmorItem.Type.BOOTS));
    private static final TagKey<Item> REPAIR_MATERIALS =
            TagKey.create(Registries.ITEM, ResourceLocation.parse("purified_undead:breathgold_repair_materials"));

    static Ingredient repairMaterial() { return Ingredient.of(REPAIR_MATERIALS); }

    public static final Tier TIER =
            new Tier() {
                public int getUses() {
                    return BreathgoldSettings.DURABILITY;
                }

                public float getSpeed() {
                    return BreathgoldSettings.MINING_SPEED;
                }

                public float getAttackDamageBonus() {
                    return BreathgoldSettings.TIER_DAMAGE;
                }

                public int getEnchantmentValue() {
                    return BreathgoldSettings.ENCHANTABILITY;
                }

                public Ingredient getRepairIngredient() {
                    return repairMaterial();
                }

                public int getLevel() {
                    return 2;
                }
            };
    public static final RegistryObject<Item> SWORD =
            ITEMS.register(
                    "breathgold_sword",
                    () ->
                            new SwordItem(
                                    TIER,
                                    BreathgoldSettings.damage("sword", 4),
                                    BreathgoldSettings.attackSpeed("sword", -2.4F),
                                    new Item.Properties()));
    public static final RegistryObject<Item> AXE =
            ITEMS.register(
                    "breathgold_axe",
                    () ->
                            new AxeItem(
                                    TIER,
                                    BreathgoldSettings.damage("axe", 6),
                                    BreathgoldSettings.attackSpeed("axe", -3.1F),
                                    new Item.Properties()));
    public static final RegistryObject<Item> HOE =
            ITEMS.register(
                    "breathgold_hoe",
                    () ->
                            new HoeItem(
                                    TIER,
                                    BreathgoldSettings.damage("hoe", -2),
                                    BreathgoldSettings.attackSpeed("hoe", -1.0F),
                                    new Item.Properties()));
    public static final RegistryObject<Item> PICKAXE =
            ITEMS.register(
                    "breathgold_pickaxe",
                    () ->
                            new PickaxeItem(
                                    TIER,
                                    BreathgoldSettings.damage("pickaxe", 1),
                                    BreathgoldSettings.attackSpeed("pickaxe", -2.8F),
                                    new Item.Properties()));
    public static final RegistryObject<Item> SHOVEL =
            ITEMS.register(
                    "breathgold_shovel",
                    () ->
                            new ShovelItem(
                                    TIER,
                                    BreathgoldSettings.shovelDamage(),
                                    BreathgoldSettings.attackSpeed("shovel", -3.0F),
                                    new Item.Properties()));

    public static void creative(java.util.function.Consumer<Item> output) {
        ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private BreathgoldContent() {}
}
