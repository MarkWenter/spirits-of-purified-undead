package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> ACCESSORIES =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "accessories"));

    private ModItemTags() {}
}
