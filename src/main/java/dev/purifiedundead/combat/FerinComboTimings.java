package dev.purifiedundead.combat;

/** Server-tick timing contract shared by later animation and hit detection. */
public record FerinComboTimings(
        int summonCooldown,
        int comboLock,
        int comboWindow,
        int finalActionDuration,
        int exitDuration) {
    public FerinComboTimings {
        if (summonCooldown < 0 || comboLock < 0 || finalActionDuration < 0 || exitDuration < 0) {
            throw new IllegalArgumentException("Ferin timings cannot be negative");
        }
        if (comboWindow <= comboLock) {
            throw new IllegalArgumentException("Combo window must end after the combo lock");
        }
    }
}
