package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HoenirModelTest {
    @Test
    void blightRaisesAmplifierAndHoenirHalvesDurationExactlyOnce() {
        assertEquals(
                new HoenirModel.EffectAdjustment(1, 201),
                HoenirModel.adjustNegativeEffect(1, 201, false, false));
        assertEquals(
                new HoenirModel.EffectAdjustment(2, 201),
                HoenirModel.adjustNegativeEffect(1, 201, true, false));
        assertEquals(
                new HoenirModel.EffectAdjustment(1, 101),
                HoenirModel.adjustNegativeEffect(1, 201, true, true));
        assertEquals(
                new HoenirModel.EffectAdjustment(1, -1),
                HoenirModel.adjustNegativeEffect(1, -1, true, true));
        assertEquals(
                new HoenirModel.EffectAdjustment(255, 20),
                HoenirModel.adjustNegativeEffect(255, 20, true, false));
    }

    @Test
    void regenerationAndOwnerMarkUseDesignRatios() {
        assertEquals(2.0F, HoenirModel.regenerationAmount(40.0F), 0.0001F);
        assertEquals(15.0F, HoenirModel.ferinDamage(10.0F, true), 0.0001F);
        assertEquals(10.0F, HoenirModel.ferinDamage(10.0F, false), 0.0001F);
        assertThrows(
                IllegalArgumentException.class,
                () -> HoenirModel.adjustNegativeEffect(-1, 20, true, false));
    }
}
