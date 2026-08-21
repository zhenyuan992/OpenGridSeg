package org.opengridseg.detect;

import static org.junit.jupiter.api.Assertions.*;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;

class OrientationEstimatorTest {
    @Test void recoversSyntheticBarAngle() {
        FloatPlane image = new FloatPlane(256, 256);
        for (int i = 0; i < image.pixels().length; i++) image.pixels()[i] = 1000f;
        double angle = 34.0;
        double a = Math.toRadians(angle), ca = Math.cos(a), sa = Math.sin(a);
        for (int gy = 40; gy <= 216; gy += 88) for (int gx = 40; gx <= 216; gx += 88) {
            for (int y = 0; y < 256; y++) for (int x = 0; x < 256; x++) {
                double dx = x - gx, dy = y - gy;
                double along = ca * dx + sa * dy;
                double across = -sa * dx + ca * dy;
                if (Math.abs(along) <= 30 && Math.abs(across) <= 9) image.set(x, y, 100f);
            }
        }
        double estimated = OrientationEstimator.estimateDegrees(image);
        assertEquals(angle, estimated, 2.0);
    }
}
