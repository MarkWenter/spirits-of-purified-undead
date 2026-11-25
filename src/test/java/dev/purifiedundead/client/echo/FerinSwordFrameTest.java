package dev.purifiedundead.client.echo;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FerinSwordFrameTest {
    @Test void bothEdgesTrackTheArcAcrossAllFiveCuts() {
        for (int stage = 1; stage <= 5; stage++) {
            Vector3f previousFace = null;
            for (int i = 0; i <= 200; i++) {
                double p = i / 200.0;
                var ray = FerinSwordFrame.ray(stage, p);
                var frame = new Quaternionf();
                FerinSwordFrame.orient(ray, FerinSwordFrame.normal(stage, p), frame);
                var tip = frame.transform(new Vector3f(0, 1, 0));
                var edge = frame.transform(new Vector3f(1, 0, 0));
                var face = frame.transform(new Vector3f(0, 0, 1));
                var tangent = FerinSwordFrame.ray(stage, Math.min(1, p + .002))
                        .sub(FerinSwordFrame.ray(stage, Math.max(0, p - .002))).normalize();
                assertTrue(tip.dot(ray) > .99999);
                assertTrue(Math.abs(edge.dot(tangent)) > .999);
                assertTrue(Math.abs(face.dot(tangent)) < .002);
                if (previousFace != null) assertTrue(face.dot(previousFace) > .99);
                previousFace = face;
            }
        }
    }
}
