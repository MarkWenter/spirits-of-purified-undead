package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UlvModelTest {
    @Test
    void attackSpeedPenaltyIsReplacedByUlvBonus() {
        assertEquals(1.0D, UlvModel.attackSpeedMultiplier(false, false));
        assertEquals(0.8D, UlvModel.attackSpeedMultiplier(true, false));
        assertEquals(1.2D, UlvModel.attackSpeedMultiplier(true, true));
    }

    @Test
    void backlashAndFollowUpUseConfirmedRatios() {
        assertEquals(1.0F, UlvModel.backlashDamage(20.0F), 0.0001F);
        assertEquals(6.0F, UlvModel.followUpDamage(12.0F), 0.0001F);
        assertThrows(IllegalArgumentException.class, () -> UlvModel.backlashDamage(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> UlvModel.followUpDamage(-1.0F));
    }
}
