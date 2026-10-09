package dev.purifiedundead.combat;

/** Final health-only arithmetic without a second hurt event. */
public final class FinalDamageRules {
    private FinalDamageRules() {}

    public static float subtractFromHealth(float amount, float absorption, float penalty) {
        float absorbed = Math.min(amount, Math.max(0.0F, absorption));
        return absorbed + Math.max(0.0F, amount - absorbed - penalty);
    }
}
