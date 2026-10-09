package dev.purifiedundead.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BlightedHealthTest {
    @Test
    void golemDoublesHealthWithoutHealingExistingInjuries() {
        assertEquals(200, BlightedHealth.rescale(100, 100, 200));
        assertEquals(80, BlightedHealth.rescale(40, 100, 200));
        assertEquals(0, BlightedHealth.rescale(0, 100, 200));
    }

    @Test
    void kingConvertsFromVanillaMaximumAndRepeatedScalingIsStable() {
        assertEquals(100, BlightedHealth.rescale(24, 24, 100));
        assertEquals(50, BlightedHealth.rescale(12, 24, 100));
        assertEquals(50, BlightedHealth.rescale(50, 100, 100));
    }

    @Test
    void malformedHealthCannotCreateInvalidAttributes() {
        assertEquals(0, BlightedHealth.rescale(Float.NaN, 100, 200));
        assertEquals(0, BlightedHealth.rescale(10, 0, 200));
        assertEquals(200, BlightedHealth.rescale(120, 100, 200));
    }
}
