package dev.purifiedundead.content;

import dev.purifiedundead.PurifiedUndead;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypeTags {
    public static final TagKey<DamageType> MAGIC = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(PurifiedUndead.MOD_ID, "magic"));

    private ModDamageTypeTags() {
    }
}
