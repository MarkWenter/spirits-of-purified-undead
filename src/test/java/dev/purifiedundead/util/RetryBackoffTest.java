package dev.purifiedundead.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetryBackoffTest {
    @Test
    void retriesRecoverWithoutBusyLoopAndRemainCapped() {
        var retry = new RetryBackoff();
        assertTrue(retry.ready(0));
        long now = 0;
        for (int delay : new int[] {20, 40, 80, 160, 320, 640, 1200, 1200, 1200}) {
            retry.failed(now);
            assertFalse(retry.ready(now + delay - 1));
            now += delay;
            assertTrue(retry.ready(now));
        }
    }
}
