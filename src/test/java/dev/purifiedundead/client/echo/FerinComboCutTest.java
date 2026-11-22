package dev.purifiedundead.client.echo;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FerinComboCutTest {
    @Test void shortTracksHoldTheirLastKeyDuringTheRestOfTheCut() {
        var clip = FerinComboCut.read(new StringReader("""
                {"animations":{"animation.ferin.test":{"bones":{
                  "MRoot":{"position":{"0":[0,0,0],"0.1":[0,8,0]}},
                  "RightArm":{"rotation":{"0":[0,0,0],"0.2":[0,0,90]}}
                }}}}
                """), "test");
        float[] arm = {0};
        EchoAnimations.Rotations pose = (joint, x, y, z) -> arm[0] = z;
        assertEquals(8F, clip.sample(3, pose));
        assertEquals(Math.toRadians(67.5), arm[0], 1e-6);
        assertEquals(8F, clip.sample(8, pose));
        assertEquals(Math.PI / 2, arm[0], 1e-6);
    }
}
