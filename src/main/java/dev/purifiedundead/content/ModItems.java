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

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
