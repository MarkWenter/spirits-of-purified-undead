package dev.purifiedundead.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FerinDynamicSlashTest {
    @Test
    void launchCompletesAndInvalidTimesAreEmpty() {
        for (int stage = 1; stage <= 5; stage++) {
            assertFalse(FerinSlashMesh.launch(stage, 1).isEmpty());
            assertTrue(FerinSlashMesh.launch(stage, 8).isEmpty());
            assertTrue(FerinSlashMesh.launch(stage, Double.NaN).isEmpty());
            assertTrue(FerinSlashMesh.flight(stage, Double.POSITIVE_INFINITY, 20).isEmpty());
        }
    }

    @Test
    void releasedBladeKeepsMovingInternallyWithoutChangingItsOuterReach() {
        for (int stage = 1; stage <= 5; stage++) {
            var early = FerinSlashMesh.flight(stage, 8, 30);
            var late = FerinSlashMesh.flight(stage, 10, 30);
            // The second radial band moves after formation, while the outer cutting curve is fixed.
            assertNotEquals(early.get(1).c().up(), late.get(1).c().up());
            var a = early.get(10).c();
            var b = late.get(10).c();
            assertEquals(a.right(), b.right(), 1e-9);
            assertEquals(a.up(), b.up(), 1e-9);
            assertEquals(a.forward(), b.forward(), 1e-9);
            for (var quad : late)
                for (var v : new FerinSlashMesh.Vertex[] {quad.a(), quad.b(), quad.c(), quad.d()})
                    assertTrue(Double.isFinite(v.right()) && v.alpha() >= 0 && v.alpha() <= 255);
        }
    }
}
