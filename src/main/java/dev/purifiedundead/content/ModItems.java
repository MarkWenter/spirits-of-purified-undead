package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import dev.purifiedundead.content.item.AncientContractItem;
import dev.purifiedundead.content.item.FerinWarriorItem;
import dev.purifiedundead.content.item.BlightedSpiritItem;
import dev.purifiedundead.content.item.GrothWarriorItem;
import dev.purifiedundead.content.item.JuliusWarriorItem;
import dev.purifiedundead.content.item.GuardianWarriorsItem;
import dev.purifiedundead.content.item.FormerOrnamentItem;
import dev.purifiedundead.content.item.SnowFlowerItem;
import dev.purifiedundead.content.item.UlvWarriorItem;
import dev.purifiedundead.content.item.TiredHeartItem;
import dev.purifiedundead.content.item.EleineWarriorItem;
import dev.purifiedundead.content.item.HoenirWarriorItem;
import dev.purifiedundead.content.item.FadenWarriorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PurifiedUndead.MOD_ID);

    public static final RegistryObject<Item> ANCIENT_CONTRACT = ITEMS.register(
            "ancient_contract", AncientContractItem::new);
    public static final RegistryObject<Item> FERIN_WARRIOR = ITEMS.register(
            "ferin_warrior", FerinWarriorItem::new);
    public static final RegistryObject<Item> PURE_CRYSTAL = ITEMS.register("pure_crystal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PURIFIED_ARCSTEEL = ITEMS.register("purified_arcsteel", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PURIFIED_ARCSTEEL_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("purified_arcsteel_upgrade_smithing_template", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLIGHT_FRAGMENT = ITEMS.register(
            "blight_fragment", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLIGHTED_SPIRIT = ITEMS.register(
            "blighted_spirit", BlightedSpiritItem::new);
    public static final RegistryObject<Item> GROTH_WARRIOR = ITEMS.register(
            "groth_warrior", GrothWarriorItem::new);
    public static final RegistryObject<Item> JULIUS_WARRIOR = ITEMS.register(
            "julius_warrior", JuliusWarriorItem::new);
    public static final RegistryObject<Item> FORMER_ORNAMENT = ITEMS.register(
            "former_ornament", FormerOrnamentItem::new);
    public static final RegistryObject<Item> GUARDIAN_WARRIORS = ITEMS.register(
            "guardian_warriors", GuardianWarriorsItem::new);
    public static final RegistryObject<Item> SNOW_FLOWER = ITEMS.register(
            "snow_flower", SnowFlowerItem::new);
    public static final RegistryObject<Item> ULV_WARRIOR = ITEMS.register(
            "ulv_warrior", UlvWarriorItem::new);
    public static final RegistryObject<Item> TIRED_HEART = ITEMS.register(
            "tired_heart", TiredHeartItem::new);
    public static final RegistryObject<Item> ELEINE_WARRIOR = ITEMS.register(
            "eleine_warrior", EleineWarriorItem::new);
    public static final RegistryObject<Item> HOENIR_WARRIOR = ITEMS.register(
            "hoenir_warrior", HoenirWarriorItem::new);
    public static final RegistryObject<Item> FADEN_WARRIOR = ITEMS.register(
            "faden_warrior", FadenWarriorItem::new);

    public static final RegistryObject<Item> BLOODSTAINED_RIBBON = ITEMS.register("bloodstained_ribbon",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.BLOODSTAINED_RIBBON));
    public static final RegistryObject<Item> ANCIENT_DRAGON_CLAW = ITEMS.register("ancient_dragon_claw",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.ANCIENT_DRAGON_CLAW));
    public static final RegistryObject<Item> WEATHERED_WARRIOR_NECKLACE = ITEMS.register("weathered_warrior_necklace",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.WEATHERED_WARRIOR_NECKLACE));
    public static final RegistryObject<Item> SOILED_SILVER_ROSARY = ITEMS.register("soiled_silver_rosary",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.SOILED_SILVER_ROSARY));
    public static final RegistryObject<Item> WHITE_PRIESTESS_STATUE = ITEMS.register("white_priestess_statue",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.WHITE_PRIESTESS_STATUE));
    public static final RegistryObject<Item> BLIGHTED_FINGER = ITEMS.register("blighted_finger",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.BLIGHTED_FINGER));
    public static final RegistryObject<Item> KINGS_SHIELD_BADGE = ITEMS.register("kings_shield_badge",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.KINGS_SHIELD_BADGE));
    public static final RegistryObject<Item> WHITE_PRIESTESS_EARRINGS = ITEMS.register("white_priestess_earrings",
            () -> new dev.purifiedundead.content.relic.DesignedRelicItem(dev.purifiedundead.content.relic.RelicKind.WHITE_PRIESTESS_EARRINGS));

    public static final RegistryObject<Item> WHITE_LOTUS = ITEMS.register("white_lotus", () -> new dev.purifiedundead.purification.PurificationConsumable(dev.purifiedundead.purification.PurificationConsumable.Kind.WHITE));
    public static final RegistryObject<Item> SCARLET_LOTUS = ITEMS.register("scarlet_lotus", () -> new dev.purifiedundead.purification.PurificationConsumable(dev.purifiedundead.purification.PurificationConsumable.Kind.SCARLET));
    public static final RegistryObject<Item> PURE_FORBIDDEN_FRUIT = ITEMS.register("pure_forbidden_fruit", () -> new dev.purifiedundead.purification.PurificationConsumable(dev.purifiedundead.purification.PurificationConsumable.Kind.FRUIT));
    public static final RegistryObject<Item> IMMACULATE_HELMET = ITEMS.register("immaculate_helmet", () -> new dev.purifiedundead.purification.ImmaculateArmor(net.minecraft.world.item.ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> IMMACULATE_CHESTPLATE = ITEMS.register("immaculate_chestplate", () -> new dev.purifiedundead.purification.ImmaculateArmor(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> IMMACULATE_LEGGINGS = ITEMS.register("immaculate_leggings", () -> new dev.purifiedundead.purification.ImmaculateArmor(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> IMMACULATE_BOOTS = ITEMS.register("immaculate_boots", () -> new dev.purifiedundead.purification.ImmaculateArmor(net.minecraft.world.item.ArmorItem.Type.BOOTS));
    public static final RegistryObject<Item> PURE_TOUCH = ITEMS.register("pure_touch", dev.purifiedundead.purification.PureTouchItem::new);
    public static final RegistryObject<Item> BLIGHTED_GUARDIAN = ITEMS.register("blighted_guardian", dev.purifiedundead.purification.BlightedGuardianItem::new);

    public static final RegistryObject<Item> LILY_DIARY = ITEMS.register("lily_diary", dev.purifiedundead.purification.LilyDiaryItem::new);

    static {
        for (String id : dev.purifiedundead.progress.ModAdvancements.IDS)
            ITEMS.register("advancement_" + id, () -> new Item(new Item.Properties()));
    }

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        dev.purifiedundead.purification.LilySmithingRecipe.register(modBus);
        ITEMS.register(modBus);
    }
}
