package dev.purifiedundead.blightedrain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BlightedRainModelTest {
    @Test
    void pausedCooldownSurvivesTimeAndRestoration() {
        var s = new BlightedRainModel();
        s.start(0, 20);
        s.stop(300);
        assertTrue(s.pauseCooldown(400));
        assertEquals(17900, s.remaining(100000));
        assertFalse(s.advance(100000));
        var restored = new BlightedRainModel();
        restored.restore(s.phase().ordinal(), s.deadline(), s.duration(), 100000);
        restored.restorePause(s.pausedCooldown());
        assertEquals(17900, restored.remaining(200000));
        restored.resumeCooldown(200000);
        assertFalse(restored.advance(217899));
        assertTrue(restored.advance(217900));
        assertTrue(restored.canRoll(true, true, 217900));
    }

    @Test
    void warningAndEventDoNotConsumeCooldown() {
        var s = new BlightedRainModel();
        s.start(100, 6000);
        assertEquals(BlightedRainModel.Phase.WARNING, s.phase());
        assertFalse(s.advance(399));
        assertTrue(s.advance(400));
        assertEquals(6400, s.deadline());
        assertFalse(s.canRoll(true, true, 420));
        assertFalse(s.advance(6399));
        assertTrue(s.advance(6400));
        assertEquals(24400, s.deadline());
        assertEquals(18000, s.remaining(6400));
        assertFalse(s.advance(24399));
        assertTrue(s.advance(24400));
        assertTrue(s.canRoll(true, true, 24400));
    }

    @Test
    void drawOnlyWhenRainyInhabitedReadyAndAtSecondBoundary() {
        var s = new BlightedRainModel();
        assertTrue(s.canRoll(true, true, 20));
        assertFalse(s.canRoll(false, true, 20));
        assertFalse(s.canRoll(true, false, 20));
        assertFalse(s.canRoll(true, true, 21));
        s.start(20, 20);
        assertFalse(s.canRoll(true, true, 40));
        s.stop(40);
        assertFalse(s.canRoll(true, true, 60));
    }

    @Test
    void residueDecaysInsteadOfImmediatelyResetting() {
        assertEquals(60, BlightedRainModel.progress(59, true, 0));
        assertEquals(59, BlightedRainModel.progress(60, false, 0));
        assertEquals(200, BlightedRainModel.progress(199, true, 0));
        assertEquals(200, BlightedRainModel.progress(200, true, 0));
        assertEquals(0, BlightedRainModel.progress(0, false, 0));
        assertEquals(1, BlightedRainModel.progress(60, true, 100));
        assertEquals(40, BlightedRainModel.progress(60, false, 19));
    }

    @Test
    void malformedSavedDeadlinesAndPhasesAreBounded() {
        var s = new BlightedRainModel();
        s.restore(99, Long.MAX_VALUE, -10, 100);
        assertEquals(BlightedRainModel.Phase.READY, s.phase());
        s.restore(1, Long.MAX_VALUE, Integer.MAX_VALUE, 100);
        assertEquals(400, s.deadline());
        assertEquals(12000, s.duration());
        s.restore(3, -1, 6000, 100);
        assertEquals(0, s.remaining(100));
        assertTrue(s.advance(100));
    }

    @Test
    void duplicateStartAndInvalidDurationsAreRejected() {
        var s = new BlightedRainModel();
        assertThrows(IllegalArgumentException.class, () -> s.start(0, 0));
        s.start(0, 20);
        assertThrows(IllegalStateException.class, () -> s.start(0, 20));
    }

    @Test
    void manualStopStartsCooldownAtStopTime() {
        var s = new BlightedRainModel();
        s.start(0, 12000);
        s.advance(300);
        s.stop(400);
        assertEquals(18400, s.deadline());
        assertEquals(18000, s.remaining(400));
        s.reset();
        assertTrue(s.canRoll(true, true, 400));
    }
}
