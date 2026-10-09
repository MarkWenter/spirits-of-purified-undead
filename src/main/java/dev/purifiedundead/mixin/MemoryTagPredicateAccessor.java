package dev.purifiedundead.mixin;

import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagPredicate.class)
public interface MemoryTagPredicateAccessor {
    @Accessor("tag")
    TagKey<DamageType> purifiedUndead$tag();

    @Accessor("expected")
    boolean purifiedUndead$expected();
}
