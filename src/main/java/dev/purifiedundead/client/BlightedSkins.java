package dev.purifiedundead.client;

public final class BlightedSkins {
    private static final java.util.Set<String> TEXTURES =
            java.util.Set.of(
                    "textures/entity/chicken.png",
                    "textures/entity/cow/brown_mooshroom.png",
                    "textures/entity/cow/cow.png",
                    "textures/entity/cow/red_mooshroom.png",
                    "textures/entity/horse/donkey.png",
                    "textures/entity/horse/horse_black.png",
                    "textures/entity/horse/horse_brown.png",
                    "textures/entity/horse/horse_chestnut.png",
                    "textures/entity/horse/horse_creamy.png",
                    "textures/entity/horse/horse_darkbrown.png",
                    "textures/entity/horse/horse_gray.png",
                    "textures/entity/horse/horse_markings_blackdots.png",
                    "textures/entity/horse/horse_markings_white.png",
                    "textures/entity/horse/horse_markings_whitedots.png",
                    "textures/entity/horse/horse_markings_whitefield.png",
                    "textures/entity/horse/horse_skeleton.png",
                    "textures/entity/horse/horse_white.png",
                    "textures/entity/horse/horse_zombie.png",
                    "textures/entity/horse/mule.png",
                    "textures/entity/illager/evoker.png",
                    "textures/entity/illager/evoker_fangs.png",
                    "textures/entity/illager/illusioner.png",
                    "textures/entity/illager/pillager.png",
                    "textures/entity/illager/ravager.png",
                    "textures/entity/illager/vex.png",
                    "textures/entity/illager/vex_charging.png",
                    "textures/entity/illager/vindicator.png",
                    "textures/entity/pig/pig.png",
                    "textures/entity/pig/pig_saddle.png",
                    "textures/entity/sheep/sheep.png",
                    "textures/entity/sheep/sheep_fur.png",
                    "textures/entity/skeleton/skeleton.png",
                    "textures/entity/skeleton/stray.png",
                    "textures/entity/skeleton/stray_overlay.png",
                    "textures/entity/skeleton/wither_skeleton.png",
                    "textures/entity/witch.png",
                    "textures/entity/zombie/drowned.png",
                    "textures/entity/zombie/drowned_outer_layer.png",
                    "textures/entity/zombie/husk.png",
                    "textures/entity/zombie/zombie.png",
                    "textures/entity/zombie_villager/profession/armorer.png",
                    "textures/entity/zombie_villager/profession/butcher.png",
                    "textures/entity/zombie_villager/profession/cartographer.png",
                    "textures/entity/zombie_villager/profession/cleric.png",
                    "textures/entity/zombie_villager/profession/farmer.png",
                    "textures/entity/zombie_villager/profession/fisherman.png",
                    "textures/entity/zombie_villager/profession/fletcher.png",
                    "textures/entity/zombie_villager/profession/leatherworker.png",
                    "textures/entity/zombie_villager/profession/librarian.png",
                    "textures/entity/zombie_villager/profession/mason.png",
                    "textures/entity/zombie_villager/profession/nitwit.png",
                    "textures/entity/zombie_villager/profession/shepherd.png",
                    "textures/entity/zombie_villager/profession/toolsmith.png",
                    "textures/entity/zombie_villager/profession/weaponsmith.png",
                    "textures/entity/zombie_villager/profession_level/diamond.png",
                    "textures/entity/zombie_villager/profession_level/emerald.png",
                    "textures/entity/zombie_villager/profession_level/gold.png",
                    "textures/entity/zombie_villager/profession_level/iron.png",
                    "textures/entity/zombie_villager/profession_level/stone.png",
                    "textures/entity/zombie_villager/type/desert.png",
                    "textures/entity/zombie_villager/type/jungle.png",
                    "textures/entity/zombie_villager/type/plains.png",
                    "textures/entity/zombie_villager/type/savanna.png",
                    "textures/entity/zombie_villager/type/snow.png",
                    "textures/entity/zombie_villager/type/swamp.png",
                    "textures/entity/zombie_villager/type/taiga.png",
                    "textures/entity/zombie_villager/zombie_villager.png");

    public static net.minecraft.resources.ResourceLocation texture(
            net.minecraft.world.entity.LivingEntity entity,
            net.minecraft.resources.ResourceLocation original) {
        if (original == null
                || !(entity instanceof dev.purifiedundead.blightedrain.mob.BlightedVariant)
                || !original.getNamespace().equals("minecraft")
                || !TEXTURES.contains(original.getPath())) return original;
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "purified_undead",
                "textures/entity/blighted/"
                        + original.getPath().substring("textures/entity/".length()));
    }

    private BlightedSkins() {}
}
