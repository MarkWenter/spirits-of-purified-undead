package dev.purifiedundead.progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WhiteWitchTalismanTest {
    @Test
    void damageMultiplierScalesFromTwentyPercentPenaltyToFortyPercentReduction() {
        assertEquals(1.20F, WhiteWitchTalisman.incomingDamageMultiplier(0), 0.0001F);
        assertEquals(1.00F, WhiteWitchTalisman.incomingDamageMultiplier(5), 0.0001F);
        assertEquals(0.80F, WhiteWitchTalisman.incomingDamageMultiplier(10), 0.0001F);
        assertEquals(0.60F, WhiteWitchTalisman.incomingDamageMultiplier(15), 0.0001F);
    }

    @Test
    void ferinStagesUnlockAtLevelsFiveAndTen() {
        assertEquals(3, WhiteWitchTalisman.ferinMaxStages(4));
        assertEquals(4, WhiteWitchTalisman.ferinMaxStages(5));
        assertEquals(4, WhiteWitchTalisman.ferinMaxStages(9));
        assertEquals(5, WhiteWitchTalisman.ferinMaxStages(10));
    }

    @Test
    void outOfRangeLevelsAreClamped() {
        assertEquals(0, WhiteWitchTalisman.clampLevel(-1));
        assertEquals(15, WhiteWitchTalisman.clampLevel(99));
    }
}
