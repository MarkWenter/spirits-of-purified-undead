package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinCriticalModelTest {
    @Test
    void rollsOnceAndReturnsOneMultiplierForTheWholeStage() {
        var critical = FerinCriticalModel.roll(0.25, 1.5, () -> 0.10);
        assertTrue(critical.critical());
        assertEquals(1.5F, critical.multiplier());

        var normal = FerinCriticalModel.roll(0.25, 1.5, () -> 0.50);
        assertFalse(normal.critical());
        assertEquals(1.0F, normal.multiplier());
    }

    @Test
    void supportsOverflowCriticalChanceWithDiminishingDamage() {
        var rolls = new double[] {0.0, 0.0, 1.0};
        var index = new int[] {0};
        var result = FerinCriticalModel.roll(1.25, 2.0, () -> rolls[index[0]++]);
        assertEquals(3.4F, result.multiplier(), 0.0001F);
    }

    @Test
    void rejectsInvalidAttributeValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FerinCriticalModel.roll(-0.1, 1.5, () -> 0.0));
        assertThrows(
                IllegalArgumentException.class, () -> FerinCriticalModel.roll(0.1, 0.9, () -> 0.0));
    }
}
