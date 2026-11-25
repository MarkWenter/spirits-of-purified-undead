package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinDamageModelTest {
    @Test
    void optionalBonusOverflowPreservesOrdinaryCriticalDamage() {
        assertEquals(24F, FerinDamageModel.withCritical(12F, Float.MAX_VALUE, 2F));
        assertEquals(30F, FerinDamageModel.withCritical(12F, 15F, 2F));
        assertEquals(0F, FerinDamageModel.withCritical(12F, 0F, 2F));
    }

    @Test
    void appliesTheConfirmedCoefficientToEachStagesOwnSnapshot() {
        assertEquals(75.0F, FerinDamageModel.baseDamage(1, 100.0F));
        assertEquals(75.0F, FerinDamageModel.baseDamage(2, 100.0F));
        assertEquals(65.0F, FerinDamageModel.baseDamage(3, 100.0F));
        assertEquals(65.0F, FerinDamageModel.baseDamage(4, 100.0F));
        assertEquals(125.0F, FerinDamageModel.baseDamage(5, 100.0F));
    }

    @Test
    void rejectsInvalidStagesAndSnapshots() {
        assertThrows(IllegalArgumentException.class, () -> FerinDamageModel.baseDamage(0, 10.0F));
        assertThrows(IllegalArgumentException.class, () -> FerinDamageModel.baseDamage(6, 10.0F));
        assertThrows(
                IllegalArgumentException.class, () -> FerinDamageModel.baseDamage(1, Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> FerinDamageModel.baseDamage(1, -1.0F));
    }
}
