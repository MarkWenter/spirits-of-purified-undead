package dev.purifiedundead.content.relic;

import dev.purifiedundead.config.PurifiedUndeadConfig;
import dev.purifiedundead.content.item.AncientContractItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.EnumMap;
import java.util.UUID;

/** Server-authoritative effects. Cooldowns belong to the wearer, never the removable item. */
public final class RelicEvents {
    private static final String DATA = "purified_undead:white_witch_relics";
    private static final UUID HEALTH_ID = UUID.fromString("68257d71-45cc-491c-a344-12e83ecc9e77");

    private static EnumMap<RelicKind, Integer> equipped(ServerPlayer player) {
        var result = new EnumMap<RelicKind, Integer>(RelicKind.class);
        if (!PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicsEnabled)) return result;
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var slots = handler.getCurios().get(AncientContractItem.WHITE_WITCH_RELIC_SLOT_ID);
            if (slots == null) return;
            var stacks = slots.getStacks();
            boolean duplicates = PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.allowDuplicateWhiteWitchRelics);
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStackInSlot(i).getItem() instanceof DesignedRelicItem relic) {
                    result.merge(relic.kind, 1, (a, b) -> duplicates ? a + b : 1);
                }
            }
        });
        return result;
    }

    private static int count(EnumMap<RelicKind, Integer> items, RelicKind kind) { return items.getOrDefault(kind, 0); }
    private static double scale() { return PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale); }
    private static CompoundTag data(ServerPlayer player) {
        var root = player.getPersistentData();
        if (!root.contains(DATA)) root.put(DATA, new CompoundTag());
        return root.getCompound(DATA);
    }

    // Apply after auxiliary attacks capture their unboosted base, so each hit is boosted once.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        var items = equipped(player);
        boolean ranged = event.getSource().is(DamageTypeTags.IS_PROJECTILE)
                || event.getSource().getDirectEntity() instanceof Projectile;
        event.setAmount((float) (event.getAmount() * RelicRules.damageMultiplier(
                count(items, RelicKind.BLIGHTED_FINGER), count(items, RelicKind.ANCIENT_DRAGON_CLAW), ranged, scale())));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onExperience(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0) return;
        int ribbons = count(equipped(player), RelicKind.BLOODSTAINED_RIBBON);
        if (ribbons == 0) return;
        var state = data(player);
        var gain = RelicRules.experience(event.getAmount(), state.getDouble("xp_remainder"), ribbons, scale());
        state.putDouble("xp_remainder", gain.remainder());
        event.setAmount(gain.amount());
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        var items = equipped(player);
        double amount = RelicRules.healthBonus(count(items, RelicKind.SOILED_SILVER_ROSARY),
                count(items, RelicKind.KINGS_SHIELD_BADGE), scale());
        var attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        var existing = attribute.getModifier(HEALTH_ID);
        if (existing != null && existing.getAmount() == amount) return;
        if (existing != null) attribute.removeModifier(HEALTH_ID);
        if (amount != 0) attribute.addTransientModifier(new AttributeModifier(HEALTH_ID,
                "White Witch relic health", amount, AttributeModifier.Operation.MULTIPLY_BASE));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    // Run before acquisition/death-retention listeners; vanilla hand-held totems already had first refusal.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDeathProtection(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        var items = equipped(player);
        var state = data(player);
        long now = player.server.overworld().getGameTime();
        RelicKind selected = null;
        for (var kind : new RelicKind[]{RelicKind.WHITE_PRIESTESS_STATUE, RelicKind.WHITE_PRIESTESS_EARRINGS}) {
            if (count(items, kind) > 0 && RelicRules.ready(now, state.getLong(kind.id))) { selected = kind; break; }
        }
        if (selected == null) return;
        int seconds = selected == RelicKind.WHITE_PRIESTESS_STATUE ? 120 : 600;
        long cooldown = Math.max(1, Math.round(seconds * 20.0 * PurifiedUndeadConfig.get(
                PurifiedUndeadConfig.VALUES.whiteWitchRelicCooldownScale)));
        state.putLong(selected.id, now + cooldown);
        event.setCanceled(true);
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.level().broadcastEntityEvent(player, (byte) 35);
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                "message.purified_undead.relic_saved", net.minecraft.network.chat.Component.translatable(
                        "item.purified_undead." + selected.id), (cooldown + 19) / 20), true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onKill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive()) return;
        int necklaces = count(equipped(player), RelicKind.WEATHERED_WARRIOR_NECKLACE);
        if (necklaces > 0) player.heal(RelicRules.killHealing(player.getHealth(), player.getMaxHealth(), necklaces, scale()));
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        var old = event.getOriginal().getPersistentData();
        if (old.contains(DATA)) event.getEntity().getPersistentData().put(DATA, old.getCompound(DATA).copy());
    }
}
