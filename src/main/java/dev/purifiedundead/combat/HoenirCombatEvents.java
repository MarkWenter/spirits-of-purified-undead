package dev.purifiedundead.combat;

import dev.purifiedundead.content.ModItems;
import dev.purifiedundead.content.ModParticles;
import dev.purifiedundead.config.PurifiedUndeadConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Abyss Guardian blight, Hoenir regeneration, and owner-scoped Ferin marks. */
public final class HoenirCombatEvents {
    private static final Field EFFECT_DURATION =
            ObfuscationReflectionHelper.findField(MobEffectInstance.class, "f_19503_");
    private static final Field EFFECT_AMPLIFIER =
            ObfuscationReflectionHelper.findField(MobEffectInstance.class, "f_19504_");
    private static final Map<UUID, MarkedTarget> MARKS = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect().getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        Equipment equipment = readEquipment(player);
        HoenirModel.EffectAdjustment adjusted =
                HoenirModel.adjustNegativeEffect(
                        instance.getAmplifier(),
                        instance.getDuration(),
                        equipment.contract,
                        equipment.hoenir);
        setInt(EFFECT_AMPLIFIER, instance, adjusted.amplifier());
        setInt(EFFECT_DURATION, instance, adjusted.duration());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (event.getAmount() <= 0.0F
                || !(source.getEntity() instanceof ServerPlayer owner)
                || !isPlayerAttack(source, owner)
                || !readEquipment(owner).reversed()
                || !isLegalTarget(owner, event.getEntity())) {
            return;
        }
        long expiry =
                owner.serverLevel().getGameTime()
                        + PurifiedUndeadConfig.get(
                                PurifiedUndeadConfig.VALUES.hoenirMarkDurationTicks);
        MARKS.compute(
                event.getEntity().getUUID(),
                (ignored, existing) -> {
                    MarkedTarget marked =
                            existing != null && existing.dimension.equals(owner.level().dimension())
                                    ? existing
                                    : new MarkedTarget(owner.level().dimension());
                    marked.ownerExpiry.put(owner.getUUID(), expiry);
                    return marked;
                });
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0
                || !readEquipment(player).reversed()) {
            return;
        }
        player.heal(HoenirModel.regenerationAmount(player.getMaxHealth()));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        Iterator<Map.Entry<UUID, MarkedTarget>> iterator = MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, MarkedTarget> entry = iterator.next();
            MarkedTarget marked = entry.getValue();
            marked.ownerExpiry.values().removeIf(expiry -> expiry < now);
            ServerLevel level = event.getServer().getLevel(marked.dimension);
            Entity found = level == null ? null : level.getEntity(entry.getKey());
            if (!(found instanceof LivingEntity target)
                    || !target.isAlive()
                    || marked.ownerExpiry.isEmpty()) {
                iterator.remove();
                continue;
            }
            if (now % 5L == 0L) {
                double yaw = Math.toRadians(target.getYRot());
                double leftX = -Math.cos(yaw) * 0.32D;
                double leftZ = -Math.sin(yaw) * 0.32D;
                level.sendParticles(
                        ModParticles.HOENIR_MARK.get(),
                        target.getX() + leftX,
                        target.getY() + target.getBbHeight() + 0.18D,
                        target.getZ() + leftZ,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D);
            }
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID owner = event.getEntity().getUUID();
        MARKS.values().forEach(marked -> marked.ownerExpiry.remove(owner));
        MARKS.values().removeIf(marked -> marked.ownerExpiry.isEmpty());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        MARKS.clear();
    }

    public static boolean hasMark(ServerPlayer owner, LivingEntity target) {
        MarkedTarget marked = MARKS.get(target.getUUID());
        if (marked == null || !marked.dimension.equals(target.level().dimension())) {
            return false;
        }
        Long expiry = marked.ownerExpiry.get(owner.getUUID());
        return expiry != null && expiry >= owner.serverLevel().getGameTime();
    }

    public static void consumeMark(ServerPlayer owner, LivingEntity target) {
        MarkedTarget marked = MARKS.get(target.getUUID());
        if (marked == null) {
            return;
        }
        marked.ownerExpiry.remove(owner.getUUID());
        if (marked.ownerExpiry.isEmpty()) {
            MARKS.remove(target.getUUID());
        }
    }

    private static void setInt(Field field, MobEffectInstance instance, int value) {
        try {
            field.setInt(instance, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Could not adjust Hoenir status effect", exception);
        }
    }

    private static boolean isPlayerAttack(DamageSource source, ServerPlayer player) {
        return source.getEntity() == player
                && source.getDirectEntity() != null
                && !source.is(ModDamageTypes.FERIN_ASSIST)
                && !source.is(ModDamageTypes.ULV_FOLLOW_UP)
                && !source.is(ModDamageTypes.ULV_BLIGHT)
                && !source.is(ModDamageTypes.ELEINE_MAGIC_ORB);
    }

    private static boolean isLegalTarget(ServerPlayer owner, LivingEntity target) {
        if (target == owner
                || !target.isAlive()
                || !target.isAttackable()
                || owner.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Player other && !owner.canHarmPlayer(other)) {
            return false;
        }
        return !(target instanceof OwnableEntity ownable
                && owner.getUUID().equals(ownable.getOwnerUUID()));
    }

    private static Equipment readEquipment(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(
                        handler ->
                                new Equipment(
                                        handler.isEquipped(ModItems.ANCIENT_CONTRACT.get()),
                                        handler.isEquipped(ModItems.HOENIR_WARRIOR.get())))
                .orElse(Equipment.NONE);
    }

    private record Equipment(boolean contract, boolean hoenir) {
        private static final Equipment NONE = new Equipment(false, false);

        private boolean reversed() {
            return contract && hoenir;
        }
    }

    private static final class MarkedTarget {
        private final net.minecraft.resources.ResourceKey<Level> dimension;
        private final Map<UUID, Long> ownerExpiry = new HashMap<>();

        private MarkedTarget(net.minecraft.resources.ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }
}
