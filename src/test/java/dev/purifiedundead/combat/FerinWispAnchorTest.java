package dev.purifiedundead.combat;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FerinWispAnchorTest {
    @Test
    void repeatedAttackSamplesDuringSprintAndTurnMatchVisualSteps() {
        var anchor = new FerinWispAnchor();
        var owner = new Vec3(20.125, 75.375, -1.875);
        var visual = FerinWispMotion.idleAnchor(owner, 0, 0, .6);
        var velocity = Vec3.ZERO;
        assertEquals(visual, anchor.update(owner, 0, 0, .6));
        for (int tick=1; tick<=12; tick++) {
            float yaw = tick < 6 ? 0 : 90;
            Vec3 step = tick < 6 ? new Vec3(0,0,.68) : new Vec3(-.68,0,0);
            owner = owner.add(step);
            velocity = velocity.lerp(step,.5);
            visual = FerinWispMotion.step(visual,owner,velocity,yaw,tick,.6);
            assertTrue(visual.distanceTo(anchor.update(owner,yaw,tick,.6)) < 1e-10);
            assertTrue(visual.distanceTo(anchor.update(owner.add(.25,0,0),yaw,tick,.6)) < 1e-10);
        }
        owner = owner.add(300,0,0);
        assertEquals(FerinWispMotion.idleAnchor(owner,90,12,.6),anchor.update(owner,90,12,.6));
    }
}
