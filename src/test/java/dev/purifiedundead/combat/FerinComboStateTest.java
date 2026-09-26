package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FerinComboStateTest {
    private static final FerinComboTimings TIMINGS = new FerinComboTimings(40, 10, 24, 16, 4);

    @Test
    void advancesOnlyInsideTheWindowAndDoesNotQueueLockedHits() {
        var state = new FerinComboState();
        assertEquals(FerinComboState.Result.STARTED, state.onQualifyingHit(0, 3, TIMINGS));
        assertEquals(FerinComboState.Result.LOCKED, state.onQualifyingHit(6, 3, TIMINGS));
        assertEquals(1, state.stage());
        assertEquals(0, state.stageStartedAt());
        assertEquals(FerinComboState.Result.ADVANCED, state.onQualifyingHit(10, 3, TIMINGS));
        assertEquals(2, state.stage());
        assertEquals(10, state.stageStartedAt());
        assertEquals(50, state.cooldownUntil());
    }

    @Test
    void timeoutExitsAndCooldownSurvivesTheEntityLifecycle() {
        var state = new FerinComboState();
        state.onQualifyingHit(0, 3, TIMINGS);
        state.tick(24, 3, TIMINGS);
        assertTrue(state.exiting());
        state.tick(28, 3, TIMINGS);
        assertEquals(0, state.stage());
        assertFalse(state.ready(39));
        assertTrue(state.ready(40));
    }

    @Test
    void finalStageCompletesInsteadOfLoopingToStageOne() {
        var state = new FerinComboState();
        state.onQualifyingHit(0, 2, TIMINGS);
        state.onQualifyingHit(10, 2, TIMINGS);
        assertEquals(FerinComboState.Result.MAX_STAGE, state.onQualifyingHit(20, 2, TIMINGS));
        state.tick(26, 2, TIMINGS);
        assertTrue(state.exiting());
        state.tick(30, 2, TIMINGS);
        assertEquals(0, state.stage());
        assertFalse(state.ready(49));
        assertTrue(state.ready(50));
    }

    @Test
    void rejectsInvalidTimingAndStageDefinitions() {
        assertThrows(IllegalArgumentException.class, () -> new FerinComboTimings(40, 10, 10, 16, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new FerinComboState().onQualifyingHit(0, 6, TIMINGS));
    }

    @Test
    void snapshotRestoresPlayerOwnedComboState() {
        var original = new FerinComboState();
        original.onQualifyingHit(100, 3, TIMINGS);
        var restored = FerinComboState.restore(original.snapshot());
        assertEquals(1, restored.stage());
        assertEquals(100, restored.stageStartedAt());
        assertEquals(140, restored.cooldownUntil());
    }
}
