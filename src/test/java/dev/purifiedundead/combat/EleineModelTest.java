package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EleineModelTest {
    @Test
    void magicPenaltyAndBonusReplaceOneAnother() {
        assertEquals(10.0F, EleineModel.incomingMagicDamage(10.0F, false, false), 0.0001F);
        assertEquals(13.0F, EleineModel.incomingMagicDamage(10.0F, true, false), 0.0001F);
        assertEquals(10.0F, EleineModel.incomingMagicDamage(10.0F, true, true), 0.0001F);
        assertEquals(13.0F, EleineModel.outgoingMagicDamage(10.0F, true, true), 0.0001F);
        assertEquals(10.0F, EleineModel.outgoingMagicDamage(10.0F, true, false), 0.0001F);
    }

    @Test
    void swimAndOrbUseRecordedInitialValues() {
        assertEquals(0.65D, EleineModel.swimSpeedMultiplier(true, false));
        assertEquals(1.0D, EleineModel.swimSpeedMultiplier(true, true));
        assertEquals(4.5F, EleineModel.orbDamage(10.0F), 0.0001F);
        assertTrue(EleineModel.rollOrb(0.2999F));
        assertFalse(EleineModel.rollOrb(0.30F));
        assertThrows(IllegalArgumentException.class, () -> EleineModel.rollOrb(1.0F));
        assertThrows(IllegalArgumentException.class, () -> EleineModel.orbDamage(Float.NaN));
    }
}
