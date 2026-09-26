package dev.purifiedundead.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PurifiedUndeadConfigTest {
    @Test
    void defaultsPreserveTheConfirmedFirstReleaseRules() {
        assertEquals(8, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.warriorSlots));
        assertEquals(3, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicSlots));
        assertEquals(15, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.talismanMaxLevel));
        assertEquals(4, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.spiritsPerTalismanLevel));
        assertEquals(List.of(0.75D, 0.75D, 0.65D, 0.65D, 1.25D),
                PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ferinStageDamageMultipliers));
        assertEquals(5, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.ulvMaxLayers));
        assertEquals(3, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.eleineDrownedKillsRequired));
        assertEquals(5, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.hoenirNegativeEffectsRequired));
    }

    @Test
    void futureRelicControlsStartEnabledAndNeutral() {
        assertTrue(PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicsEnabled));
        assertEquals(1.0D, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicEffectScale));
        assertEquals(1.0D, PurifiedUndeadConfig.get(PurifiedUndeadConfig.VALUES.whiteWitchRelicCooldownScale));
    }
}
