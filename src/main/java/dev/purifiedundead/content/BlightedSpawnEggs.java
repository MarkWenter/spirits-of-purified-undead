package dev.purifiedundead.content;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;

import java.util.function.Supplier;

public final class BlightedSpawnEggs {
    private static final net.minecraftforge.registries.DeferredRegister<Item> ITEMS =
            net.minecraftforge.registries.DeferredRegister.create(
                    net.minecraft.core.registries.Registries.ITEM, "purified_undead");
    public static final Supplier<Item> TAB_ICON =
            ITEMS.register("creative_tab_icon", () -> new Item(new Item.Properties()));
    private static final java.util.List<Supplier<? extends Item>> EGGS =
            new java.util.ArrayList<>();

    static {
        egg(
                "blighted_spider",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.SPIDER,
                net.minecraft.world.entity.EntityType.SPIDER);
        egg(
                "blighted_zombie",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.ZOMBIE,
                net.minecraft.world.entity.EntityType.ZOMBIE);
        egg(
                "blighted_skeleton",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.SKELETON,
                net.minecraft.world.entity.EntityType.SKELETON);
        egg(
                "blighted_drowned",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.DROWNED,
                net.minecraft.world.entity.EntityType.DROWNED);
        egg(
                "blighted_husk",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.HUSK,
                net.minecraft.world.entity.EntityType.HUSK);
        egg(
                "blighted_stray",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.STRAY,
                net.minecraft.world.entity.EntityType.STRAY);
        egg(
                "blighted_villager",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.VILLAGER,
                net.minecraft.world.entity.EntityType.VILLAGER);
        egg(
                "blighted_pillager",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.PILLAGER,
                net.minecraft.world.entity.EntityType.PILLAGER);
        egg(
                "blighted_vindicator",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.VINDICATOR,
                net.minecraft.world.entity.EntityType.VINDICATOR);
        egg(
                "blighted_witch",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.WITCH,
                net.minecraft.world.entity.EntityType.WITCH);
        egg(
                "blighted_pig",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.PIG,
                net.minecraft.world.entity.EntityType.PIG);
        egg(
                "blighted_cow",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.COW,
                net.minecraft.world.entity.EntityType.COW);
        egg(
                "blighted_sheep",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.SHEEP,
                net.minecraft.world.entity.EntityType.SHEEP);
        egg(
                "blighted_chicken",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.CHICKEN,
                net.minecraft.world.entity.EntityType.CHICKEN);
        egg(
                "blighted_horse",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.HORSE,
                net.minecraft.world.entity.EntityType.HORSE);
        egg(
                "blighted_donkey",
                dev.purifiedundead.blightedrain.mob.BlightedMobContent.DONKEY,
                net.minecraft.world.entity.EntityType.DONKEY);
        egg(
                "blighted_golem",
                ModEntities.BLIGHTED_GOLEM,
                net.minecraft.world.entity.EntityType.IRON_GOLEM);
        egg(
                "blighted_king",
                ModEntities.BLIGHTED_KING,
                net.minecraft.world.entity.EntityType.EVOKER);
    }

    private static void egg(
            String name,
            Supplier<? extends EntityType<? extends Mob>> type,
            EntityType<?> nativeType) {
        EGGS.add(
                ITEMS.register(
                        name + "_spawn_egg",
                        () -> {
                            SpawnEggItem vanilla = SpawnEggItem.byId(nativeType);
                            int base = vanilla == null ? 0xD4D1BB : vanilla.getColor(0);
                            int spots = vanilla == null ? 0x7B8069 : vanilla.getColor(1);
                            return new net.minecraftforge.common.ForgeSpawnEggItem(
                                    type, base, spots, new Item.Properties()) {
                                @Override
                                public int getColor(int layer) {
                                    return layer >= 2 ? 0xFFFFFF : super.getColor(layer);
                                }
                            };
                        }));
    }

    public static void creative(java.util.function.Consumer<Item> output) {
        EGGS.forEach(egg -> output.accept(egg.get()));
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        ITEMS.register(bus);
    }

    private BlightedSpawnEggs() {}
}
