package dev.purifiedundead.blightedarmaments;

import net.minecraft.core.registries.Registries;
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
            ITEMS.register("breathgold_ore", () -> new BlockItem(ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> MATERIAL =
            ITEMS.register("breathgold", BreathgoldItem::new);
    public static final Tier TIER =
            new Tier() {
                public int getUses() {
                    return 375;
                }

                public float getSpeed() {
                    return 6F;
                }

                public float getAttackDamageBonus() {
                    return 2F;
                }

                public int getEnchantmentValue() {
                    return 14;
                }

                public Ingredient getRepairIngredient() {
                    return Ingredient.of(MATERIAL.get());
                }

                public int getLevel() {
                    return 2;
                }
            };
    public static final RegistryObject<Item> SWORD =
            ITEMS.register(
                    "breathgold_sword", () -> new SwordItem(TIER, 4, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> AXE =
            ITEMS.register(
                    "breathgold_axe", () -> new AxeItem(TIER, 6, -3.1F, new Item.Properties()));
    public static final RegistryObject<Item> HOE =
            ITEMS.register(
                    "breathgold_hoe", () -> new HoeItem(TIER, -2, -1.0F, new Item.Properties()));
    public static final RegistryObject<Item> PICKAXE =
            ITEMS.register(
                    "breathgold_pickaxe",
                    () -> new PickaxeItem(TIER, 1, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> SHOVEL =
            ITEMS.register(
                    "breathgold_shovel",
                    () -> new ShovelItem(TIER, 1.5F, -3.0F, new Item.Properties()));

    public static void creative(java.util.function.Consumer<Item> output) {
        ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private BreathgoldContent() {}
}
