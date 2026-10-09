package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinBlightModelTest {
    @Test
    void contractMeleePenaltyIsReplacedByWarriorBonus() {
        assertEquals(10.0F, FerinBlightModel.meleeDamage(10.0F, false, false), 0.0001F);
        assertEquals(7.0F, FerinBlightModel.meleeDamage(10.0F, true, false), 0.0001F);
        assertEquals(13.0F, FerinBlightModel.meleeDamage(10.0F, true, true), 0.0001F);
        assertEquals(10.0F, FerinBlightModel.meleeDamage(10.0F, false, true), 0.0001F);
    }

    @Test
    void lifestealUsesActualHealthLossAndCapsOverkill() {
        assertEquals(2.0F, FerinBlightModel.lifesteal(10.0F, 20.0F), 0.0001F);
        assertEquals(1.0F, FerinBlightModel.lifesteal(10.0F, 5.0F), 0.0001F);
        assertEquals(0.0F, FerinBlightModel.lifesteal(0.0F, 20.0F), 0.0001F);
    }

    @Test
    void invalidDamageIsRejectedOrSafelyIgnored() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FerinBlightModel.meleeDamage(Float.NaN, true, false));
        assertEquals(0.0F, FerinBlightModel.lifesteal(Float.NaN, 20.0F), 0.0001F);
    }

    @Test
    void ulvBonusAddsInTheSameMeleeLayer() {
        assertEquals(18.0F, FerinBlightModel.meleeDamage(10.0F, true, true, 0.50D), 0.0001F);
        assertEquals(12.0F, FerinBlightModel.meleeDamage(10.0F, true, false, 0.50D), 0.0001F);
        assertThrows(
                IllegalArgumentException.class,
                () -> FerinBlightModel.meleeDamage(10.0F, true, true, Double.NaN));
    }
}
