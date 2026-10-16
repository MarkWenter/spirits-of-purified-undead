package dev.purifiedundead.blightedrain.mob;

public final class BlightedMobRules {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>>
            BLIGHTED =
                    net.minecraft.tags.TagKey.create(
                            net.minecraft.core.registries.Registries.ENTITY_TYPE,
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                                    "purified_undead", "blighted_creatures"));

    public static boolean faster(net.minecraft.world.entity.LivingEntity entity) {
        return entity instanceof BlightedVariant && !slowArcher(entity);
    }

    public static int interval(int original, net.minecraft.world.entity.LivingEntity entity) {
        return slowArcher(entity)
                ? (int) Math.ceil(original / .7)
                : faster(entity) ? Math.max(1, original / 2) : original;
    }

    public static boolean slowArcher(net.minecraft.world.entity.LivingEntity e) {
        return e instanceof BlightedVariant
                && e instanceof net.minecraft.world.entity.monster.AbstractSkeleton;
    }

    public static boolean adjusted(net.minecraft.world.entity.LivingEntity e) {
        return slowArcher(e)
                || e instanceof BlightedZombie
                || e instanceof BlightedHusk
                || e instanceof BlightedDrowned
                || e instanceof BlightedVillager;
    }

    public static double healthFactor(net.minecraft.world.entity.LivingEntity e) {
        return adjusted(e) || e instanceof BlightedSpider ? 1.5 : 2.0;
    }

    public static double movementFactor(net.minecraft.world.entity.LivingEntity e) {
        return e instanceof BlightedSpider
                ? .8
                : e instanceof BlightedVillager ? .5 : adjusted(e) ? .7 : 1;
    }

    public static int drawTicks(int ticks, net.minecraft.world.entity.LivingEntity e) {
        return slowArcher(e) ? (int) (ticks * .7) : faster(e) ? ticks * 2 : ticks;
    }

    public static float attackDamage(
            net.minecraft.world.damagesource.DamageSource source, float amount) {
        var attacker = source.getEntity();
        if (!(attacker instanceof net.minecraft.world.entity.LivingEntity e)
                || !adjusted(e)
                || e instanceof BlightedVillager) return amount;
        return amount * 1.2F + (e instanceof BlightedHusk ? 1F : 0F);
    }

    public static void afterDamage(
            net.minecraft.world.entity.LivingEntity target,
            net.minecraft.world.damagesource.DamageSource source,
            float amount) {
        if (amount > 0
                && !target.level().isClientSide
                && source.getEntity() instanceof BlightedSpider spider
                && spider.getRandom().nextFloat() < .1F)
            target.addEffect(
                    new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.WEAKNESS, 200, 0));
        if (amount > 0
                && !target.level().isClientSide
                && source.getEntity() instanceof BlightedDrowned)
            target.addEffect(
                    new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 200, 0));
    }

    public static void readLegacy(
            net.minecraft.world.entity.LivingEntity e, net.minecraft.nbt.CompoundTag data) {
        if (!data.contains("Attributes", 9) || data.getInt("BlightedTraitsVersion") >= 1) return;
        float ratio = e.getMaxHealth() > 0 ? e.getHealth() / e.getMaxHealth() : 1;
        var health = e.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        health.setBaseValue(health.getBaseValue() * .75);
        for (var modifier : java.util.List.copyOf(health.getModifiers())) {
            if (modifier.getOperation()
                    == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation
                            .ADDITION) {
                health.removeModifier(modifier);
                health.addPermanentModifier(
                        new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                modifier.getId(),
                                modifier.getName(),
                                modifier.getAmount() * .75,
                                modifier.getOperation()));
            }
        }
        var speed =
                e.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        speed.setBaseValue(e instanceof BlightedVillager ? .25 : speed.getBaseValue() * .7);
        e.setHealth(ratio * e.getMaxHealth());
    }

    private BlightedMobRules() {}
}
