package dev.purifiedundead.blightedarmaments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ImmaculateTools {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, "purified_undead");
    public static final RegistryObject<Item> SWORD =
            ITEMS.register("immaculate_sword", ImmaculateTools::sword);
    public static final RegistryObject<Item> SHOVEL =
            ITEMS.register("immaculate_shovel", ImmaculateTools::shovel);
    public static final RegistryObject<Item> AXE =
            ITEMS.register("immaculate_axe", ImmaculateTools::axe);
    public static final RegistryObject<Item> HOE =
            ITEMS.register("immaculate_hoe", ImmaculateTools::hoe);
    public static final RegistryObject<Item> PICKAXE =
            ITEMS.register("immaculate_pickaxe", ImmaculateTools::pickaxe);

    static final ArmamentProperties CONFIG =
            new ArmamentProperties("purified_undead-immaculate-tools.properties");
    static final int DURABILITY = CONFIG.integer("durability", 2031, 1, 1000000);
    static final int BONUS_DAMAGE = CONFIG.integer("extraDamage", 1, 0, 1024);
    static final int LOG_REPAIR = CONFIG.integer("logRepair", 1, 0, 1000000);
    static final boolean SEAL = CONFIG.integer("swordSeal", 1, 0, 1) == 1;
    public static final float SOIL_CHANCE = CONFIG.decimal("soilChance", 0.3F, 0, 1);
    public static final float CROP_CHANCE = CONFIG.decimal("cropChance", 0.3F, 0, 1);
    public static final float ORE_CHANCE = CONFIG.decimal("oreChance", 0.3F, 0, 1);
    public static final TagKey<Block> SOILS = blocks("soils"),
            LOGS = blocks("logs"),
            CROPS = blocks("crops"),
            ORES = blocks("ores");

    private static TagKey<Block> blocks(String group) {
        return TagKey.create(
                Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("purified_undead", "immaculate_" + group));
    }

    private static final Tier TIER = Tiers.DIAMOND;

    static Item sword() {
        return new SwordItem(
                TIER, 3 + BONUS_DAMAGE, -2.4F, new Item.Properties().durability(DURABILITY));
    }

    static Item shovel() {
        return new ShovelItem(
                TIER, 1.5F + BONUS_DAMAGE, -3F, new Item.Properties().durability(DURABILITY));
    }

    static Item axe() {
        return new AxeItem(
                TIER, 5 + BONUS_DAMAGE, -3F, new Item.Properties().durability(DURABILITY)) {
            @Override
            public boolean mineBlock(
                    ItemStack stack,
                    Level level,
                    BlockState state,
                    BlockPos pos,
                    LivingEntity miner) {
                if (!state.is(LOGS) || LOG_REPAIR == 0)
                    return super.mineBlock(stack, level, state, pos, miner);
                if (!level.isClientSide && stack.isDamaged())
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - LOG_REPAIR));
                return true;
            }
        };
    }

    static Item hoe() {
        return new HoeItem(
                TIER, -3 + BONUS_DAMAGE, 0F, new Item.Properties().durability(DURABILITY));
    }

    static Item pickaxe() {
        return new PickaxeItem(
                TIER, 1 + BONUS_DAMAGE, -2.8F, new Item.Properties().durability(DURABILITY));
    }

    static void register(IEventBus bus) {
        ITEMS.register(bus);
        ImmaculateSeals.register();
    }

    public static void creative(java.util.function.Consumer<Item> output) {
        ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
    }

    private ImmaculateTools() {}
}
