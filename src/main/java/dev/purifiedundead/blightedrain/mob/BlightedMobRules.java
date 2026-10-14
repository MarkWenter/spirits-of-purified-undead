package dev.purifiedundead.blightedrain.mob;

public final class BlightedMobRules {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>>
            BLIGHTED =
                    net.minecraft.tags.TagKey.create(
                            net.minecraft.core.registries.Registries.ENTITY_TYPE,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    "purified_undead", "blighted_creatures"));

    public static boolean faster(net.minecraft.world.entity.LivingEntity entity) {
        return entity instanceof BlightedVariant;
    }

    public static int interval(int original, net.minecraft.world.entity.LivingEntity entity) {
        return faster(entity) ? Math.max(1, original / 2) : original;
    }

    private BlightedMobRules() {}
}
