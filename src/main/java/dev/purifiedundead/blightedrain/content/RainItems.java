package dev.purifiedundead.blightedrain.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

public final class RainItems {
    private static final net.minecraftforge.registries.DeferredRegister<Item> ITEMS =
            net.minecraftforge.registries.DeferredRegister.create(
                    Registries.ITEM, "purified_undead");
    private static final net.minecraftforge.registries.DeferredRegister<Block> BLOCKS =
            net.minecraftforge.registries.DeferredRegister.create(
                    Registries.BLOCK, "purified_undead");
    public static final net.minecraftforge.registries.RegistryObject<Block> BEACON_BLOCK =
            BLOCKS.register("still_rain_beacon", StillRainBeaconBlock::new);
    public static final net.minecraftforge.registries.RegistryObject<Item> BEACON =
            ITEMS.register(
                    "still_rain_beacon",
                    () -> new BlockItem(BEACON_BLOCK.get(), new Item.Properties()));
    public static final net.minecraftforge.registries.RegistryObject<Item> EMBLEM =
            ITEMS.register("blighted_rain_emblem", RainEmblemItem::new);
    public static final net.minecraftforge.registries.RegistryObject<Item> DUST =
            ITEMS.register("grave_mire_dust", () -> new Item(new Item.Properties()));
    public static final net.minecraftforge.registries.RegistryObject<Item> RUBY =
            ITEMS.register("blighted_ruby", () -> new Item(new Item.Properties()));
    public static final net.minecraftforge.registries.RegistryObject<Item> MEAT =
            ITEMS.register("blighted_meat", BlightedMeatItem::new);
    public static final net.minecraftforge.registries.RegistryObject<Item> COOKED_MEAT =
            ITEMS.register(
                    "cooked_blighted_meat",
                    () ->
                            new Item(
                                    new Item.Properties()
                                            .food(
                                                    new net.minecraft.world.food.FoodProperties
                                                                    .Builder()
                                                            .nutrition(8)
                                                            .saturationMod(.25F)
                                                            .meat()
                                                            .build())));
    public static final net.minecraftforge.registries.RegistryObject<Item> SHIELD =
            ITEMS.register("headless_knight", HeadlessKnightItem::new);
    private static final net.minecraftforge.registries.DeferredRegister<EntityType<?>> ENTITIES =
            net.minecraftforge.registries.DeferredRegister.create(
                    Registries.ENTITY_TYPE, "purified_undead");
    public static final net.minecraftforge.registries.RegistryObject<
                    EntityType<CliffYouthProjectile>>
            YOUTH_PROJECTILE =
                    ENTITIES.register(
                            "cliff_youth_projectile",
                            () ->
                                    EntityType.Builder.<CliffYouthProjectile>of(
                                                    CliffYouthProjectile::new, MobCategory.MISC)
                                            .sized(.5F, .5F)
                                            .clientTrackingRange(8)
                                            .updateInterval(1)
                                            .build("purified_undead:cliff_youth_projectile"));
    public static final net.minecraftforge.registries.RegistryObject<Item> YOUTH =
            ITEMS.register("cliff_village_youth", CliffYouthItem::new);

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }

    public static void creative(
            java.util.function.Consumer<net.minecraft.world.level.ItemLike> out) {
        for (var item : ITEMS.getEntries()) out.accept(item.get());
    }

    private RainItems() {}
}
