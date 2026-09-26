package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FadenModelTest {
    @Test
    void fortuneAndLootingGainOneEffectiveLevelIncludingFromZero() {
        assertEquals(0, FadenModel.effectiveEnchantmentLevel(0, false));
        assertEquals(1, FadenModel.effectiveEnchantmentLevel(0, true));
        assertEquals(4, FadenModel.effectiveEnchantmentLevel(3, true));
        assertThrows(IllegalArgumentException.class,
                () -> FadenModel.effectiveEnchantmentLevel(-1, true));
    }

    @Test
    void poisonRollUsesTwentyPercentBoundary() {
        assertTrue(FadenModel.rollPoison(0.0F));
        assertTrue(FadenModel.rollPoison(0.19999F));
        assertFalse(FadenModel.rollPoison(0.20F));
        assertThrows(IllegalArgumentException.class, () -> FadenModel.rollPoison(1.0F));
    }
}
