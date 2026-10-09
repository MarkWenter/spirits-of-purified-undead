package dev.purifiedundead.slate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MemoryJumpTest {
    @Test
    void addsHalfBlockAcrossJumpBoostsAndGravity() {
        for (double v : new double[] {.42, .52, .82, 1.22})
            for (double g : new double[] {.04, .08, .16})
                assertEquals(
                        MemoryJump.height(v, g) + .5,
                        MemoryJump.height(MemoryJump.raise(v, g, .5), g),
                        1e-6);
    }

    @Test
    void doesNotInventJumpUnderZeroGravity() {
        assertEquals(.42, MemoryJump.raise(.42, 0, .5));
        assertEquals(-.1, MemoryJump.raise(-.1, .08, .5));
    }
}
