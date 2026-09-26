package dev.purifiedundead.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FerinVisualModelTest {
    @Test
    void everyCombatStageHasAStableAnimationName() {
        for (int stage = 1; stage <= 5; stage++) {
            assertEquals("animation.ferin.stage_" + stage, FerinVisualModel.animationForStage(stage));
        }
        assertEquals("animation.ferin.idle", FerinVisualModel.animationForStage(0));
        assertEquals("animation.ferin.idle", FerinVisualModel.animationForStage(6));
    }
}
