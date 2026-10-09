package dev.purifiedundead.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerinContinuationBufferTest {
    @Test
    void earlyInputIsConsumedOnceAtUnlock() {
        var b = new FerinContinuationBuffer();
        b.request(1, 100);
        assertFalse(b.consume(1, 100, 105, 10, true));
        assertTrue(b.consume(1, 100, 110, 10, true));
        assertFalse(b.consume(2, 110, 120, 10, true));
    }

    @Test
    void spamQueuesOnlyOneStageAndOldInputCannotCrossRestart() {
        var b = new FerinContinuationBuffer();
        for (int i = 0; i < 100; i++) b.request(1, 100);
        assertTrue(b.consume(1, 100, 110, 10, true));
        assertFalse(b.consume(1, 100, 111, 10, true));
        b.request(1, 100);
        assertFalse(b.consume(1, 200, 210, 10, true));
    }

    @Test
    void invalidOrExpiredComboDropsIntent() {
        var b = new FerinContinuationBuffer();
        b.request(2, 100);
        assertFalse(b.consume(2, 100, 110, 10, false));
        assertFalse(b.consume(2, 100, 111, 10, true));
    }

    @Test
    void heldOrRepeatedAttackCanContinueAllFiveWithoutRepeatedHits() {
        var state = new FerinComboState();
        var b = new FerinContinuationBuffer();
        var timings = new FerinComboTimings(40, 10, 40, 16, 4);
        state.onQualifyingHit(0, 5, timings);
        for (int stage = 1; stage < 5; stage++) {
            long start = state.stageStartedAt();
            b.request(stage, start);
            state.tick(start + 10, 5, timings);
            assertTrue(b.consume(stage, start, start + 10, 10, true));
            assertEquals(
                    FerinComboState.Result.ADVANCED, state.onQualifyingHit(start + 10, 5, timings));
        }
        assertEquals(5, state.stage());
        assertEquals(FerinComboState.Result.MAX_STAGE, state.onQualifyingHit(50, 5, timings));
    }

    @Test
    void relaxedWindowAcceptsLateInputWithoutAutoContinuing() {
        var state = new FerinComboState();
        var timings = new FerinComboTimings(40, 10, 40, 16, 4);
        state.onQualifyingHit(0, 5, timings);
        state.tick(30, 5, timings);
        assertEquals(1, state.stage());
        assertFalse(state.exiting());
        assertEquals(FerinComboState.Result.ADVANCED, state.onQualifyingHit(35, 5, timings));
    }
}
