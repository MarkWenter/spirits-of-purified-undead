package dev.purifiedundead.blightedarmaments;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Persistent keys belong solely to this entity. No ticking, world index or per-effect scan on
 * application.
 */
public final class ImmaculateSeals {
    private static final String KEY = "purified_undead:immaculate_seals";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMelee(LivingDamageEvent event) {
        var target = event.getEntity();
        var source = event.getSource();
        if (!ImmaculateTools.SEAL
                || target.level().isClientSide
                || !(event.getAmount() > 0)
                || !(source.getDirectEntity() instanceof LivingEntity attacker)
                || attacker != source.getEntity()
                || !attacker.getMainHandItem().is(ImmaculateTools.SWORD.get())) return;
        if (source.is(DamageTypes.PLAYER_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) strike(target);
    }

    private static void strike(LivingEntity target) {
        for (var instance : target.getActiveEffects()) {
            var effect = instance.getEffect();
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) continue;
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (id == null) continue;
            var data = target.getPersistentData();
            var seals = data.getCompound(KEY);
            String key = id.toString();
            // Install the seal before removal callbacks so an immediate reapplication is rejected.
            boolean previous = seals.getBoolean(key);
            seals.putBoolean(key, true);
            data.put(KEY, seals);
            try {
                target.removeEffect(effect);
            } finally {
                if (target.hasEffect(effect) && !previous) {
                    seals.remove(key);
                    if (seals.isEmpty()) data.remove(KEY);
                }
            }
            return;
        }
    }

    public static boolean isSealed(LivingEntity target, ResourceLocation effect) {
        var data = target.getPersistentData();
        return data.contains(KEY, Tag.TAG_COMPOUND)
                && data.getCompound(KEY).getBoolean(effect.toString());
    }

    private static boolean denies(MobEffectEvent event) {
        var target = event.getEntity();
        if (target.level().isClientSide
                || !target.getPersistentData().contains(KEY, Tag.TAG_COMPOUND)) return false;
        var effect = event.getEffectInstance().getEffect();
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        return id != null && isSealed(target, id);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applicable(MobEffectEvent.Applicable event) {
        if (denies(event)) event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        var data = event.getOriginal().getPersistentData();
        if (data.contains(KEY, Tag.TAG_COMPOUND))
            event.getEntity().getPersistentData().put(KEY, data.getCompound(KEY).copy());
    }

    static void register() {
        var bus = net.minecraftforge.common.MinecraftForge.EVENT_BUS;
        bus.register(ImmaculateSeals.class);
        if (!net.minecraftforge.fml.ModList.get().isLoaded("l2library")) return;
        // L2's forced path writes directly to activeEffects after its own veto event.
        try {
            var forced =
                    Class.forName(
                                    "dev.xkmc.l2library.base.effects.ForceAddEffectEvent",
                                    false,
                                    ImmaculateSeals.class.getClassLoader())
                            .asSubclass(MobEffectEvent.class);
            bus.addListener(
                    EventPriority.LOWEST,
                    false,
                    forced,
                    event -> {
                        if (denies(event))
                            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                    });
        } catch (ClassNotFoundException | ClassCastException incompatible) {
            com.mojang.logging.LogUtils.getLogger()
                    .warn("L2 forced effect hook unavailable", incompatible);
        }
    }

    private ImmaculateSeals() {}
}
