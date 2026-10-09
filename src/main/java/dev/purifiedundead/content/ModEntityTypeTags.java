package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> BLIGHT_FRAGMENT_SOURCES =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            PurifiedUndead.MOD_ID, "blight_fragment_sources"));

    private ModEntityTypeTags() {}
}
