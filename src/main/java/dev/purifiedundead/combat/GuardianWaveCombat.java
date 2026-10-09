package dev.purifiedundead.combat;

import dev.purifiedundead.content.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Server-owned swing snapshot; a scoped context supplies delayed melee state without mutating the player. */
public final class GuardianWaveCombat {
    public record State(
            ServerPlayer owner,
            boolean falling,
            boolean sprinting,
            float strength,
            float baseDamage,
            float critical) {}

    private static final ThreadLocal<State> ACTIVE = new ThreadLocal<>();
    private static final String LAST = "purified_undead:guardian_wave_tick";

    public static boolean active() {
        return ACTIVE.get() != null;
    }

    public static boolean falling(Player p) {
        var s = ACTIVE.get();
        return s != null && s.owner() == p ? s.falling() : p.fallDistance > 0 && !p.onGround();
    }

    public static boolean sprinting(Player p) {
        var s = ACTIVE.get();
        return s != null && s.owner() == p ? s.sprinting() : p.isSprinting();
    }

    public static void with(State state, Runnable hit) {
        var previous = ACTIVE.get();
        ACTIVE.set(state);
        try {
            hit.run();
        } finally {
            if (previous == null) ACTIVE.remove();
            else ACTIVE.set(previous);
        }
    }

    public static void swing(ServerPlayer p) {
        swing(p, 0);
    }

    public static void swing(ServerPlayer p, int shot) {
        if (shot < 0) return;
        if (!p.isAlive()
                || p.isSpectator()
                || p.isUsingItem()
                || !p.getMainHandItem().is(ModItems.BLIGHTED_GUARDIAN.get())
                || p.hasEffect(ModEffects.STUNNED.get())) return;
        long now = p.serverLevel().getGameTime();
        var data = p.getPersistentData();
        if (data.contains(LAST) && data.getLong(LAST) == now) return;
        float strength = p.getAttackStrengthScale(0.5F);
        if (data.contains(LAST) && now >= data.getLong(LAST))
            strength =
                    Math.min(
                            strength,
                            net.minecraft.util.Mth.clamp(
                                    (now - data.getLong(LAST) + 0.5F)
                                            / p.getCurrentItemAttackStrengthDelay(),
                                    0F,
                                    1F));
        data.putLong(LAST, now);
        float base =
                (float)
                        p.getAttributeValue(
                                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (!Float.isFinite(base) || base <= 0) return;
        boolean falling = p.fallDistance > 0 && !p.onGround();
        boolean vanillaCrit =
                falling
                        && strength > 0.9F
                        && !p.isSprinting()
                        && !p.onClimbable()
                        && !p.isInWater()
                        && !p.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
                        && !p.isPassenger();
        base *= 0.2F + strength * strength * 0.8F;
        if (vanillaCrit) base *= 1.5F;
        var wave = ModEntities.GUARDIAN_WAVE.get().create(p.serverLevel());
        if (wave != null) {
            wave.launch(
                    new State(
                            p,
                            falling,
                            p.isSprinting(),
                            strength,
                            base,
                            FerinCombatEvents.rollStageCritical(p).multiplier()),
                    p.getMainHandItem().copy(),
                    shot);
            p.serverLevel().addFreshEntity(wave);
        }
    }

    private GuardianWaveCombat() {}
}
