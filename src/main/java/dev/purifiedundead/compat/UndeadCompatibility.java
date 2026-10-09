package dev.purifiedundead.compat;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.ModEntityTypeTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/** Uses vanilla contracts and optional data tags; never links against another mod's classes. */
public final class UndeadCompatibility {
    public static final TagKey<EntityType<?>> EXCLUSIONS =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            "purified_undead", "blight_fragment_exclusions"));

    public static boolean fragmentSource(LivingEntity entity) {
        if (entity.getType().is(EXCLUSIONS)) return false;
        if (entity.getType().is(ModEntityTypeTags.BLIGHT_FRAGMENT_SOURCES)) return true;
        return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.autoDetectModdedUndeadDrops)
                && !BuiltInRegistries.ENTITY_TYPE
                        .getKey(entity.getType())
                        .getNamespace()
                        .equals("minecraft")
                && entity instanceof Enemy
                && entity.getMobType() == net.minecraft.world.entity.MobType.UNDEAD;
    }

    private UndeadCompatibility() {}
}
